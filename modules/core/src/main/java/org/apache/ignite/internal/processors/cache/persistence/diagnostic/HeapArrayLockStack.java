package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;
import org.apache.ignite.internal.util.typedef.internal.SB;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockStack implements PageLockListener {
    private int headIdx;

    //Benchmark                                     (stackSize)              (type)   Mode  Cnt          Score         Error  Units
    //JmhPageListenerLockStackBenchmark.lockUnlock            2  HeapArrayLockStack  thrpt   10  112220367.940 ± 3489526.518  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            4  HeapArrayLockStack  thrpt   10   70455560.182 ± 3312950.796  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            8  HeapArrayLockStack  thrpt   10   44866501.588 ±  624434.909  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock           16  HeapArrayLockStack  thrpt   10   24397179.687 ±  469696.820  ops/s

    //Benchmark                                     (stackSize)              (type)   Mode  Cnt         Score         Error  Units
    //JmhPageListenerLockStackBenchmark.lockUnlock            2  HeapArrayLockStack  thrpt   10   9633344.414 ±  728236.796  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            4  HeapArrayLockStack  thrpt   10   4852075.852 ±  255843.100  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            8  HeapArrayLockStack  thrpt   10   2399365.642 ±   68528.195  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock           16  HeapArrayLockStack  thrpt   10   1165728.828 ±  120575.800  ops/s

    //Benchmark                                     (stackSize)              (type)   Mode  Cnt         Score        Error  Units
    //JmhPageListenerLockStackBenchmark.lockUnlock            2  HeapArrayLockStack  thrpt   10  14279323.516 ± 141363.335  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            4  HeapArrayLockStack  thrpt   10   7076342.539 ±  90446.056  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            8  HeapArrayLockStack  thrpt   10   3546973.366 ±  59300.908  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock           16  HeapArrayLockStack  thrpt   10   1759634.526 ±  19412.998  ops/s

    //Benchmark                                     (stackSize)              (type)   Mode  Cnt         Score         Error  Units
    //JmhPageListenerLockStackBenchmark.lockUnlock            2  HeapArrayLockStack  thrpt   10  13143263.758 ± 1241703.543  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            4  HeapArrayLockStack  thrpt   10   6666108.001 ±   94336.397  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock            8  HeapArrayLockStack  thrpt   10   3235242.435 ±  147932.557  ops/s
    //JmhPageListenerLockStackBenchmark.lockUnlock           16  HeapArrayLockStack  thrpt   10   1680152.792 ±   12182.147  ops/s

    private static final int READ_LOCK = 1;
    private static final int READ_UNLOCK = 2;
    private static final int WRITE_LOCK = 3;
    private static final int WRITE_UNLOCK = 4;
    private static final int BEFORE_READ_LOCK = 5;
    private static final int BEFORE_WRITE_LOCK = 6;

    private final long[] arrPageIds = new long[64];

    private final String name;

    private long nextPage;
    private int op;

    private AtomicBoolean locked = new AtomicBoolean();

    public HeapArrayLockStack(String name) {
        this.name = "name=" + name;
    }

    @Override public void onBeforeWriteLock(int cacheId, long pageId, long page) {
        lock();

        try {
            nextPage = pageId;
            op = BEFORE_WRITE_LOCK;
        }
        finally {
            unLock();
        }
    }

    @Override public void onWriteLock(int cacheId, long pageId, long page, long pageAddr) {
        lock();

        try {
            push(cacheId, pageId, WRITE_LOCK);
        }
        finally {
            unLock();
        }
    }

    @Override public void onWriteUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lock();

        try {
            pop(cacheId, pageId, WRITE_UNLOCK);
        }
        finally {
            unLock();
        }
    }

    @Override public void onBeforeReadLock(int cacheId, long pageId, long page) {
        lock();

        try {
            nextPage = pageId;
            op = BEFORE_READ_LOCK;
        }
        finally {
            unLock();
        }
    }

    @Override public void onReadLock(int cacheId, long pageId, long page, long pageAddr) {
        lock();

        try {
            push(cacheId, pageId, READ_LOCK);
        }
        finally {
            unLock();
        }
    }

    @Override public void onReadUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lock();

        try {
            pop(cacheId, pageId, READ_UNLOCK);
        }
        finally {
            unLock();
        }
    }

    private void push(int cacheId, long pageId, int flags) {
        reset();

        assert pageId > 0;

        if (headIdx + 1 > arrPageIds.length)
            throw new StackOverflowError("Stack overflow, size:" + arrPageIds.length);

        long pageId0 = arrPageIds[headIdx];

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        arrPageIds[headIdx] = pageId;

        headIdx++;
    }

    private void lock() {
        while (!locked.compareAndSet(false, true)) {
            // Busy wait.
        }
    }

    private void unLock() {
        boolean res = locked.compareAndSet(true, false);

        assert res;
    }

    private void reset() {
        nextPage = 0;
        op = 0;
    }

    private void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        reset();

        if (headIdx > 1) {
            int last = headIdx - 1;

            long val = arrPageIds[last];

            if (val == pageId) {
                arrPageIds[last] = 0;

                //Reset head to the first not empty element.
                do {
                    headIdx--;
                }
                while (headIdx - 1 >= 0 && arrPageIds[headIdx - 1] == 0);
            }
            else {
                for (int i = last - 1; i >= 0; i--) {
                    if (arrPageIds[i] == pageId) {
                        arrPageIds[i] = 0;

                        return;
                    }
                }

                assert false : "Never should happened since with obtaine lock (push to stack) before unlock.\n"
                    + toString();
            }
        }
        else {
            long val = arrPageIds[0];

            if (val == 0)
                throw new NoSuchElementException(
                    "Stack is empty, can not pop elemnt with cacheId=" +
                        cacheId + ", pageId=" + pageId + ", flags=" + hexInt(flags));

            if (val == pageId) {
                for (int i = 0; i < headIdx; i++)
                    arrPageIds[i] = 0;

                headIdx = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        SB res = new SB();

        long[] stack;
        int headIdx;
        long nextPage;
        int op;

        lock();

        try {
            stack = Arrays.copyOf(arrPageIds, arrPageIds.length);
            headIdx = this.headIdx;
            nextPage = this.nextPage;
            op = this.op;
        }
        finally {
            unLock();
        }

        res.a(name).a(", locked pages stack:\n");

        if (nextPage != 0) {
            String str = "N/A";

            if (op == BEFORE_READ_LOCK)
                str = "obtain read lock";
            else if (op == BEFORE_WRITE_LOCK)
                str = "obtain write lock";

            res.a("\t>>> try " + str + ", " + pageIdToString(nextPage) + "\n");
        }

        for (int i = headIdx - 1; i >= 0; i--) {
            long pageId = stack[i];

            if (pageId == 0 && i == 0)
                break;

            if (pageId == 0) {
                res.a("\t" + i + " -\n");
            }
            else {
                res.a("\t" + i + " " + pageIdToString(pageId) + "\n");
            }
        }

        res.a("\n");

        return res.toString();
    }

    private String pageIdToString(long pageId) {
        return "pageId=" + pageId
            + " [pageIdxHex=" + hexLong(pageId)
            + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
            + ", flags=" + hexInt(flag(pageId)) + "]";
    }
}

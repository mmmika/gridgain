package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;
import org.apache.ignite.internal.util.typedef.internal.SB;

import static java.util.Arrays.copyOf;
import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockStack implements PageLockListener {
    private static final int READ_LOCK = 1;
    private static final int READ_UNLOCK = 2;
    private static final int WRITE_LOCK = 3;
    private static final int WRITE_UNLOCK = 4;
    private static final int BEFORE_READ_LOCK = 5;
    private static final int BEFORE_WRITE_LOCK = 6;

    private static final int STACK_SIZE = 128;

    private final String name;

    private int headIdx;

    private final long[] arrPageIds;

    private long nextPage;
    private int op;

    private volatile boolean dump;

    private volatile boolean locked;

    public HeapArrayLockStack(String name) {
        this.name = "name=" + name;
        this.arrPageIds = new long[STACK_SIZE];
    }

    @Override public void onBeforeWriteLock(int cacheId, long pageId, long page) {
        checkDump();

        locked = true;
        if (dump) {
            locked = false;

            onBeforeWriteLock(cacheId, pageId, page);

            return;
        }

        nextPage = pageId;
        op = BEFORE_WRITE_LOCK;
        locked = false;
    }

    @Override public void onWriteLock(int cacheId, long pageId, long page, long pageAddr) {
        checkDump();

        locked = true;
        if (dump) {
            locked = false;

            onWriteLock(cacheId, pageId, page, pageAddr);

            return;
        }

        push(cacheId, pageId, WRITE_LOCK);
        locked = false;
    }

    @Override public void onWriteUnlock(int cacheId, long pageId, long page, long pageAddr) {
        checkDump();

        locked = true;
        if (dump) {
            locked = false;

            onWriteUnlock(cacheId, pageId, page, pageAddr);

            return;
        }

        pop(cacheId, pageId, WRITE_UNLOCK);

        locked = false;
    }

    @Override public void onBeforeReadLock(int cacheId, long pageId, long page) {
        checkDump();

        locked = true;
        if (dump) {
            locked = false;

            onBeforeReadLock(cacheId, pageId, page);

            return;
        }

        nextPage = pageId;
        op = BEFORE_READ_LOCK;
        locked = false;
    }

    @Override public void onReadLock(int cacheId, long pageId, long page, long pageAddr) {
        checkDump();

        locked = true;

        if (dump) {
            locked = false;

            onReadLock(cacheId, pageId, page, pageAddr);
            return;
        }

        push(cacheId, pageId, READ_LOCK);
        locked = false;
    }

    @Override public void onReadUnlock(int cacheId, long pageId, long page, long pageAddr) {
        checkDump();

        locked = true;

        if (dump) {
            locked = false;

            onReadUnlock(cacheId, pageId, page, pageAddr);
            return;
        }

        pop(cacheId, pageId, READ_UNLOCK);
        locked = false;
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

        State state = dump();

        long[] stack = state.arrPageIds;
        int headIdx = state.headIdx;
        long nextPage = state.nextPage;
        int op = state.op;

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

    private void checkDump() {
        while (dump) {
            // Busy wait.
        }
    }

    public State dump() {
        dump = true;

        while (locked) {
            // Busy wait.
        }

        long[] stack = copyOf(this.arrPageIds, this.arrPageIds.length);
        State state = new State(this.name + " (time=" + System.currentTimeMillis() + ")", stack);
        state.headIdx = headIdx;
        state.nextPage = nextPage;
        state.op = op;

        dump = false;

        return state;
    }

    public static class State {
        private final String name;

        private int headIdx;

        private final long[] arrPageIds;

        private long nextPage;
        private int op;

        public State(String name, long[] arrPageIds) {
            this.name = name;
            this.arrPageIds = arrPageIds;
        }
    }

    private String pageIdToString(long pageId) {
        return "pageId=" + pageId
            + " [pageIdxHex=" + hexLong(pageId)
            + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
            + ", flags=" + hexInt(flag(pageId)) + "]";
    }
}

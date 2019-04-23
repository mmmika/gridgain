package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.NoSuchElementException;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;
import org.apache.ignite.internal.util.GridUnsafe;

import static java.util.Arrays.copyOf;
import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class OffHeapLockStack implements PageLockListener {
    private static final int READ_LOCK = 1;
    private static final int READ_UNLOCK = 2;
    private static final int WRITE_LOCK = 3;
    private static final int WRITE_UNLOCK = 4;
    private static final int BEFORE_READ_LOCK = 5;
    private static final int BEFORE_WRITE_LOCK = 6;

    private static final int STACK_SIZE = 128;

    private final String name;

    private final long ptr;

    private long headIdx;

    private long nextPage;
    private int op;

    private volatile boolean dump;

    private volatile boolean locked;

    public OffHeapLockStack(String name) {
        this.name = name;
        this.ptr = allocate(STACK_SIZE);
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

        if (headIdx + 1 > STACK_SIZE)
            throw new StackOverflowError("Stack overflow, size:" + STACK_SIZE);

        long pageId0 = pageIdByIndex(headIdx);

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        pageIdByIndex(headIdx, pageId);

        headIdx++;
    }

    private long pageIdByIndex(long headIdx) {
        return GridUnsafe.getLong(ptr + offset(headIdx));
    }

    private void pageIdByIndex(long headIdx, long pageId) {
        GridUnsafe.putLong(ptr + offset(headIdx), pageId);
    }

    private long offset(long headIdx) {
        return headIdx * 8;
    }

    private void reset() {
        nextPage = 0;
        op = 0;
    }

    private void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        reset();

        if (headIdx > 1) {
            long last = headIdx - 1;

            long pageId0 = pageIdByIndex(last);

            if (pageId0 == pageId) {
                pageIdByIndex(last, 0);

                //Reset head to the first not empty element.
                do {
                    headIdx--;
                }
                while (headIdx - 1 >= 0 && pageIdByIndex(headIdx - 1) == 0);
            }
            else {
                for (long idx = last - 1; idx >= 0; idx--) {
                    if (pageIdByIndex(idx) == pageId) {
                        pageIdByIndex(idx, 0);

                        return;
                    }
                }

                assert false : "Never should happened since with obtaine lock (push to stack) before unlock.\n"
                    + toString();
            }
        }
        else {
            long val = pageIdByIndex(0);

            if (val == 0)
                throw new NoSuchElementException(
                    "Stack is empty, can not pop elemnt with cacheId=" +
                        cacheId + ", pageId=" + pageId + ", flags=" + hexInt(flags));

            if (val == pageId) {
                for (int idx = 0; idx < headIdx; idx++)
                    pageIdByIndex(idx, 0);

                headIdx = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }

    private long allocate(int size) {
        return GridUnsafe.allocateMemory(size);
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
        long[] stack = new long[STACK_SIZE];

        GridUnsafe.copyMemory(null, ptr, stack, 0, STACK_SIZE);

        State state = new State(this.name + " (time=" + System.currentTimeMillis() + ")", stack);
        state.headIdx = headIdx;
        state.nextPage = nextPage;
        state.op = op;

        dump = false;

        return state;
    }

    public static class State {
        private final String name;

        private long headIdx;

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

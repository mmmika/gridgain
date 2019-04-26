package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.LocksStackSnapshot;
import org.apache.ignite.internal.util.GridUnsafe;

import static java.util.Arrays.copyOf;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;

public class OffHeapLockStack extends AbstractPageLockTracker<LocksStackSnapshot> {
    private static final int CAPACITY = 128;
    private static final int STACK_SIZE = CAPACITY * 8;

    private final long ptr;

    private long headIdx;

    private long nextOpPageId;
    private int nextOp;

    public OffHeapLockStack(String name) {
        super(name);

        this.ptr = allocate(STACK_SIZE);
    }

    @Override protected void onBeforeWriteLock0(int cacheId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_WRITE_LOCK;
    }

    @Override protected void onWriteLock0(int cacheId, long pageId, long page, long pageAddr) {
        push(cacheId, pageId, WRITE_LOCK);
    }

    @Override protected void onWriteUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        pop(cacheId, pageId, WRITE_UNLOCK);
    }

    @Override protected void onBeforeReadLock0(int cacheId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_READ_LOCK;
    }

    @Override protected void onReadLock0(int cacheId, long pageId, long page, long pageAddr) {
        push(cacheId, pageId, READ_LOCK);
    }

    @Override protected void onReadUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        pop(cacheId, pageId, READ_UNLOCK);
    }

    private void push(int cacheId, long pageId, int flags) {
        reset();

        assert pageId > 0;

        if (headIdx + 1 > STACK_SIZE)
            throw new StackOverflowError("Stack overflow, size:" + STACK_SIZE);

        long pageId0 = setPageId(headIdx);

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        setPageId(headIdx, pageId);

        headIdx++;
    }

    private long setPageId(long headIdx) {
        return GridUnsafe.getLong(ptr + offset(headIdx));
    }

    private void setPageId(long headIdx, long pageId) {
        GridUnsafe.putLong(ptr + offset(headIdx), pageId);
    }

    private long offset(long headIdx) {
        return headIdx * 8;
    }

    private void reset() {
        nextOpPageId = 0;
        nextOp = 0;
    }

    private void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        reset();

        if (headIdx > 1) {
            long last = headIdx - 1;

            long pageId0 = setPageId(last);

            if (pageId0 == pageId) {
                setPageId(last, 0);

                //Reset head to the first not empty element.
                do {
                    headIdx--;
                }
                while (headIdx - 1 >= 0 && setPageId(headIdx - 1) == 0);
            }
            else {
                for (long idx = last - 1; idx >= 0; idx--) {
                    if (setPageId(idx) == pageId) {
                        setPageId(idx, 0);

                        return;
                    }
                }

                assert false : "Never should happened since with obtaine lock (push to stack) before unlock.\n"
                    + toString();
            }
        }
        else {
            long val = setPageId(0);

            if (val == 0)
                throw new NoSuchElementException(
                    "Stack is empty, can not pop elemnt with cacheId=" +
                        cacheId + ", pageId=" + pageId + ", flags=" + hexInt(flags));

            if (val == pageId) {
                for (int idx = 0; idx < headIdx; idx++)
                    setPageId(idx, 0);

                headIdx = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }

    private long allocate(int size) {
        long ptr = GridUnsafe.allocateMemory(size);

        GridUnsafe.setMemory(ptr, STACK_SIZE, (byte)0);

        return ptr;
    }

    @Override public LocksStackSnapshot dump0() {
        LongBuffer buf = LongBuffer.allocate(CAPACITY);

        GridUnsafe.copyMemory(null, ptr, buf.array(), GridUnsafe.LONG_ARR_OFF, CAPACITY);

        int headIdx = (int)this.headIdx;
        int nextOp = this.nextOp;
        long nextOpPageId = this.nextOpPageId;

        long[] stack = buf.array();

        assert stack.length == CAPACITY;

        return new LocksStackSnapshot(
            name,
            System.currentTimeMillis(),
            headIdx,
            stack,
            nextOpPageId,
            nextOp
        );
    }
}

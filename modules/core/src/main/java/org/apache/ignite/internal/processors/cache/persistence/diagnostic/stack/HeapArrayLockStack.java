package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import java.util.NoSuchElementException;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.LocksStackSnapshot;

import static java.util.Arrays.copyOf;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;

public class HeapArrayLockStack extends AbstractPageLockTracker<LocksStackSnapshot> {
    private static final int STACK_SIZE = 128;

    protected int headIdx;

    private final long[] pageIdLocksStack;

    private long nextOpPageId;
    private int nextOp;

    public HeapArrayLockStack(String name) {
        super("name=" + name);
        this.pageIdLocksStack = new long[STACK_SIZE];
    }

    @Override public void onBeforeWriteLock0(int cacheId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_WRITE_LOCK;
    }

    @Override public void onWriteLock0(int cacheId, long pageId, long page, long pageAddr) {
        push(cacheId, pageId, WRITE_LOCK);
    }

    @Override public void onWriteUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        pop(cacheId, pageId, WRITE_UNLOCK);
    }

    @Override public void onBeforeReadLock0(int cacheId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_READ_LOCK;
    }

    @Override public void onReadLock0(int cacheId, long pageId, long page, long pageAddr) {
        push(cacheId, pageId, READ_LOCK);
    }

    @Override public void onReadUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        pop(cacheId, pageId, READ_UNLOCK);
    }

    private void push(int cacheId, long pageId, int flags) {
        reset();

        assert pageId > 0;

        if (headIdx + 1 > pageIdLocksStack.length)
            throw new StackOverflowError("Stack overflow, size:" + pageIdLocksStack.length);

        long pageId0 = pageIdLocksStack[headIdx];

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        pageIdLocksStack[headIdx] = pageId;

        headIdx++;
    }

    private void reset() {
        nextOpPageId = 0;
        nextOp = 0;
    }

    private void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        reset();

        if (headIdx > 1) {
            int last = headIdx - 1;

            long val = pageIdLocksStack[last];

            if (val == pageId) {
                pageIdLocksStack[last] = 0;

                //Reset head to the first not empty element.
                do {
                    headIdx--;
                }
                while (headIdx - 1 >= 0 && pageIdLocksStack[headIdx - 1] == 0);
            }
            else {
                for (int i = last - 1; i >= 0; i--) {
                    if (pageIdLocksStack[i] == pageId) {
                        pageIdLocksStack[i] = 0;

                        return;
                    }
                }

                assert false : "Never should happened since with obtaine lock (push to stack) before unlock.\n"
                    + toString();
            }
        }
        else {
            long val = pageIdLocksStack[0];

            if (val == 0)
                throw new NoSuchElementException(
                    "Stack is empty, can not pop elemnt with cacheId=" +
                        cacheId + ", pageId=" + pageId + ", flags=" + hexInt(flags));

            if (val == pageId) {
                for (int i = 0; i < headIdx; i++)
                    pageIdLocksStack[i] = 0;

                headIdx = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }


    /** {@inheritDoc} */
    @Override public LocksStackSnapshot dump0() {
        long[] stack = copyOf(this.pageIdLocksStack, this.pageIdLocksStack.length);

        return new LocksStackSnapshot(
            this.name,
            System.currentTimeMillis(),
            headIdx,
            stack,
            nextOpPageId,
            nextOp
        );
    }
}

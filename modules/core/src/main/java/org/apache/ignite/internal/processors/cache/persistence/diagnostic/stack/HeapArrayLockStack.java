package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.LocksStackSnapshot;

import static java.util.Arrays.copyOf;

public class HeapArrayLockStack extends AbstractPageLockTracker<LocksStackSnapshot> {
    private static final int STACK_SIZE = 128;

    protected int headIdx;

    private final long[] pageIdLocksStack;

    private int nextOp;
    private int nextOpStructureId;
    private long nextOpPageId;

    public HeapArrayLockStack(String name) {
        super("name=" + name);
        this.pageIdLocksStack = new long[STACK_SIZE];
    }

    @Override public void onBeforeWriteLock0(int structureId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_WRITE_LOCK;
    }

    @Override public void onWriteLock0(int structureId, long pageId, long page, long pageAddr) {
        push(structureId, pageId, WRITE_LOCK);
    }

    @Override public void onWriteUnlock0(int structureId, long pageId, long page, long pageAddr) {
        pop(structureId, pageId, WRITE_UNLOCK);
    }

    @Override public void onBeforeReadLock0(int structureId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_READ_LOCK;
    }

    @Override public void onReadLock0(int structureId, long pageId, long page, long pageAddr) {
        push(structureId, pageId, READ_LOCK);
    }

    @Override public void onReadUnlock0(int structureId, long pageId, long page, long pageAddr) {
        pop(structureId, pageId, READ_UNLOCK);
    }

    private void push(int structureId, long pageId, int flags) {
        reset(flags);

        if (headIdx + 1 > pageIdLocksStack.length) {
            invalid("Stack overflow, size=" + pageIdLocksStack.length +
                ", headIdx=" + headIdx + ", " + argsToString(structureId, pageId, flags));

            return;
        }

        long pageId0 = pageIdLocksStack[headIdx];

        if (pageId0 != 0L) {
            invalid("Head should be empty, headIdx=" + headIdx +
                ", pageIdOnHead=" + pageId0 + ", " + argsToString(structureId, pageId, flags));

            return;
        }

        pageIdLocksStack[headIdx] = pageId;

        headIdx++;
    }

    private void reset(int flags) {
        if (flags != READ_LOCK || flags != WRITE_LOCK) {
            //TODO
        }

        nextOpPageId = 0;
        nextOp = 0;
        nextOpStructureId = 0;
    }

    private void pop(int structureId, long pageId, int flags) {
        reset(flags);

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

                invalid("Can not find pageId in stack, " + argsToString(structureId, pageId, flags));
            }
        }
        else {
            if (headIdx <= 0) {
                invalid("HeadIdx can not be less or equals that zero, headIdx="
                    + headIdx + ", " + argsToString(structureId, pageId, flags));

                return;
            }

            long val = pageIdLocksStack[0];

            if (val == 0) {
                invalid("Stack is empty, can not pop elemnt, " + argsToString(structureId, pageId, flags));

                return;
            }

            if (val == pageId) {
                pageIdLocksStack[0] = 0;
                headIdx = 0;
            }
            else
                invalid("Can not find pageId in stack, " + argsToString(structureId, pageId, flags));
        }
    }

    /** {@inheritDoc} */
    @Override public LocksStackSnapshot dump0() {
        long[] stack = copyOf(pageIdLocksStack, pageIdLocksStack.length);

        return new LocksStackSnapshot(
            name,
            System.currentTimeMillis(),
            headIdx,
            stack,
            nextOp,
            nextOpStructureId,
            nextOpPageId
        );
    }
}

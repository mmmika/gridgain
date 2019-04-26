package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.LocksStackSnapshot;

public abstract class AbstractLockStack extends AbstractPageLockTracker<LocksStackSnapshot> {
    protected int headIdx;

    protected int nextOp;
    protected int nextOpStructureId;
    protected long nextOpPageId;

    protected AbstractLockStack(String name) {
        super(name);
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

        if (headIdx + 1 > capacity()) {
            invalid("Stack overflow, size=" + capacity() +
                ", headIdx=" + headIdx + ", " + argsToString(structureId, pageId, flags));

            return;
        }

        long pageId0 = pageByIndex(headIdx);

        if (pageId0 != 0L) {
            invalid("Head should be empty, headIdx=" + headIdx +
                ", pageIdOnHead=" + pageId0 + ", " + argsToString(structureId, pageId, flags));

            return;
        }

        setPageToIndex(headIdx, pageId);

        headIdx++;
    }

    protected abstract int capacity();

    protected abstract long pageByIndex(int headIdx);

    protected abstract void setPageToIndex(int headIdx, long pageId);

    private void reset(int flags) {
        if (flags != READ_LOCK || flags != WRITE_LOCK) {
            //TODO asserts
        }

        nextOpPageId = 0;
        nextOp = 0;
        nextOpStructureId = 0;
    }

    private void pop(int structureId, long pageId, int flags) {
        reset(flags);

        if (headIdx > 1) {
            int last = headIdx - 1;

            long val = pageByIndex(last);

            if (val == pageId) {
                setPageToIndex(last, 0);

                //Reset head to the first not empty element.
                do {
                    headIdx--;
                }
                while (headIdx - 1 >= 0 && pageByIndex(headIdx - 1) == 0);
            }
            else {
                for (int idx = last - 1; idx >= 0; idx--) {
                    if (pageByIndex(idx) == pageId) {
                        setPageToIndex(idx, 0);

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

            long val = pageByIndex(0);

            if (val == 0) {
                invalid("Stack is empty, can not pop elemnt, " + argsToString(structureId, pageId, flags));

                return;
            }

            if (val == pageId) {
                setPageToIndex(0, 0);

                headIdx = 0;
            }
            else
                invalid("Can not find pageId in stack, " + argsToString(structureId, pageId, flags));
        }
    }
}

package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.LocksStackSnapshot;

import static java.util.Arrays.copyOf;

public class HeapArrayLockStack extends AbstractLockStack {
    private static final int STACK_SIZE = 128;

    private final long[] pageIdLocksStack;

    public HeapArrayLockStack(String name) {
        super("name=" + name);

        this.pageIdLocksStack = new long[STACK_SIZE];
    }

    @Override protected int capacity() {
        return STACK_SIZE;
    }

    @Override protected long pageByIndex(int headIdx) {
        return pageIdLocksStack[headIdx];
    }

    @Override protected void setPageToIndex(int headIdx, long pageId) {
        pageIdLocksStack[headIdx] = pageId;
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

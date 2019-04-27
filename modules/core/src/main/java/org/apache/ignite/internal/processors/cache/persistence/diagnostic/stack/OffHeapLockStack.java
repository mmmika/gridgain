package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import java.nio.LongBuffer;
import org.apache.ignite.internal.util.GridUnsafe;

import static java.util.Arrays.copyOf;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;

public class OffHeapLockStack extends LockStack {
    private static final int CAPACITY = 128;
    private static final int STACK_SIZE = CAPACITY * 8;

    private final long ptr;

    public OffHeapLockStack(String name) {
        super(name);

        this.ptr = allocate(STACK_SIZE);
    }

    @Override public int capacity() {
        return CAPACITY;
    }

    @Override protected long getByIndex(int idx) {
        return GridUnsafe.getLong(ptr + offset(idx));
    }

    @Override protected void setByIndex(int idx, long val) {
        GridUnsafe.putLong(ptr + offset(idx), val);
    }

    private long offset(long headIdx) {
        return headIdx * 8;
    }

    private long allocate(int size) {
        long ptr = GridUnsafe.allocateMemory(size);

        GridUnsafe.setMemory(ptr, STACK_SIZE, (byte)0);

        return ptr;
    }

    @Override public LocksStackSnapshot dump0() {
        LongBuffer buf = LongBuffer.allocate(STACK_SIZE);

        GridUnsafe.copyMemory(null, ptr, buf.array(), GridUnsafe.LONG_ARR_OFF, STACK_SIZE);

        long[] stack = buf.array();

        assert stack.length == STACK_SIZE;

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

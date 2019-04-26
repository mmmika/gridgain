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

public class OffHeapLockStack extends AbstractLockStack {
    private static final int CAPACITY = 128;
    private static final int STACK_SIZE = CAPACITY * 8;

    private final long ptr;

    public OffHeapLockStack(String name) {
        super("name=" + name);

        this.ptr = allocate(STACK_SIZE);
    }

    @Override protected int capacity() {
        return CAPACITY;
    }

    @Override protected long pageByIndex(int headIdx) {
        return GridUnsafe.getLong(ptr + offset(headIdx));
    }

    @Override protected void setPageToIndex(int headIdx, long pageId) {
        GridUnsafe.putLong(ptr + offset(headIdx), pageId);
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
        LongBuffer buf = LongBuffer.allocate(CAPACITY);

        GridUnsafe.copyMemory(null, ptr, buf.array(), GridUnsafe.LONG_ARR_OFF, CAPACITY);

        long[] stack = buf.array();

        assert stack.length == CAPACITY;

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

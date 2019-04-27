package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import java.nio.LongBuffer;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.PageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.Dump;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack.LocksStackSnapshot;
import org.apache.ignite.internal.util.GridUnsafe;

public class OffHeapLockLog extends LockLog {
    private static final int CAPACITY = 128;
    private static final int STACK_SIZE = (CAPACITY * 8) * 2;

    private final long ptr;

    protected OffHeapLockLog(String name) {
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

    @Override public LockLogSnapshot dump0() {
        LongBuffer buf = LongBuffer.allocate(STACK_SIZE);

        GridUnsafe.copyMemory(null, ptr, buf.array(), GridUnsafe.LONG_ARR_OFF, STACK_SIZE);

        long[] lockLog = new long[CAPACITY];
        long[] meta = new long[CAPACITY];

        for (int i = 0; i < buf.capacity(); i += 2) {
            lockLog[i] = buf.get();
            meta[i] = buf.get();
        }

        return new LockLogSnapshot(
            name,
            System.currentTimeMillis(),
            headIdx,
            lockLog,
            meta,
            nextOp,
            nextOpStructureId,
            nextOpPageId
        );
    }
}

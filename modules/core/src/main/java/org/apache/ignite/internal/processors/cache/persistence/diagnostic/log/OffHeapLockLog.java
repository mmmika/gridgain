package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import java.nio.LongBuffer;
import org.apache.ignite.internal.util.GridUnsafe;

public class OffHeapLockLog extends LockLog {
    private static final int LOG_CAPACITY = 128;
    private static final int LOG_SIZE = (LOG_CAPACITY * 8) * 2;

    private final long ptr;

    protected OffHeapLockLog(String name) {
        super(name);

        this.ptr = allocate(LOG_SIZE);
    }

    @Override public int capacity() {
        return LOG_CAPACITY;
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

        GridUnsafe.setMemory(ptr, LOG_SIZE, (byte)0);

        return ptr;
    }

    @Override public LockLogSnapshot dump0() {
        LongBuffer buf = LongBuffer.allocate(LOG_SIZE);

        GridUnsafe.copyMemory(null, ptr, buf.array(), GridUnsafe.LONG_ARR_OFF, LOG_SIZE);

        long[] lockLog = buf.array();

        return new LockLogSnapshot(
            name,
            System.currentTimeMillis(),
            headIdx,
            lockLog,
            nextOp,
            nextOpStructureId,
            nextOpPageId
        );
    }
}

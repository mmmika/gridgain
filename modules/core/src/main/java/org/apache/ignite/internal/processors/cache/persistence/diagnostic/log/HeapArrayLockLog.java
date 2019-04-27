package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import static java.util.Arrays.copyOf;

public class HeapArrayLockLog extends LockLog {
    private static final int LOG_SIZE = 128;

    private final long[] pageIdsLockLog = new long[LOG_SIZE * 2];

    public HeapArrayLockLog(String name) {
        super(name);
    }

    @Override public int capacity() {
        return LOG_SIZE;
    }

    @Override protected long getByIndex(int idx) {
        return pageIdsLockLog[idx];
    }

    @Override protected void setByIndex(int idx, long val) {
        pageIdsLockLog[idx] = val;
    }

    @Override public LockLogSnapshot dump0() {
        long[] lockLog = copyOf(pageIdsLockLog, pageIdsLockLog.length);

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

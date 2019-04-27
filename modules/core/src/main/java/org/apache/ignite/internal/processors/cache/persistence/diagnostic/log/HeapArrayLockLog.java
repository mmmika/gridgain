package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import static java.util.Arrays.copyOf;

public class HeapArrayLockLog extends LockLog {
    private static final int STACK_SIZE = 128;

    private final long[] pageIdsLockLog = new long[STACK_SIZE * 2];

    public HeapArrayLockLog(String name) {
        super("name=" + name);
    }

    @Override public int capacity() {
        return STACK_SIZE;
    }

    @Override protected long getByIndex(int idx) {
        return pageIdsLockLog[idx];
    }

    @Override protected void setByIndex(int idx, long val) {
        pageIdsLockLog[idx] = val;
    }

    @Override public synchronized LockLogSnapshot dump0() {
        long[] lockLog = new long[STACK_SIZE];
        long[] meta = new long[STACK_SIZE];

        for (int i = 0; i < pageIdsLockLog.length; i += 2) {
            lockLog[i] = pageIdsLockLog[i];
            meta[i] = pageIdsLockLog[i + 1];
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

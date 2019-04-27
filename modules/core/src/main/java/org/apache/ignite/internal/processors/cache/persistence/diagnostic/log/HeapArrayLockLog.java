package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import static java.util.Arrays.copyOf;

public class HeapArrayLockLog extends LockLog {

    private final long[] pageIdsLockLog = new long[1024 * 2];

    public HeapArrayLockLog(String name) {
        super("name=" + name);
    }

    @Override public int capacity() {
        return 0;
    }

    @Override protected long getByIndex(int idx) {
        return 0;
    }

    @Override protected void setByIndex(int idx, long val) {

    }

    @Override public synchronized LockLogSnapshot dump0() {
        long[] pageIdsLockLog = copyOf(this.pageIdsLockLog, this.pageIdsLockLog.length);

        return new LockLogSnapshot(
            name + " (time=" + System.currentTimeMillis() + ")",
            headIdx,
            pageIdsLockLog,
            nextOp,
            nextOpStructureId,
            nextOpPageId
        );
    }
}

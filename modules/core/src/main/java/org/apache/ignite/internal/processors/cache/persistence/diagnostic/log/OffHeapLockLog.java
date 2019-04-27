package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.PageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.Dump;

public class OffHeapLockLog extends LockLog {

    protected OffHeapLockLog(String name) {
        super(name);
    }

    @Override public int capacity() {
        return 0;
    }

    @Override protected long getByIndex(int idx) {
        return 0;
    }

    @Override protected void setByIndex(int idx, long val) {

    }

    @Override protected synchronized LockLogSnapshot dump0() {
        return null;
    }
}

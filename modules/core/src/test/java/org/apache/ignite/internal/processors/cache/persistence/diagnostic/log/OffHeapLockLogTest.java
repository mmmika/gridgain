package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.PageLockLogTest;

public class OffHeapLockLogTest extends PageLockLogTest {

    @Override protected LockLog createLogStackTracer(String name) {
        return new OffHeapLockLog("Thread=" + name);
    }
}
package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.PageLockLogTest;

public class HeapArrayLockLogTest extends PageLockLogTest {

    @Override protected LockLog createLogStackTracer(String name) {
        return new HeapArrayLockLog("Thread=" + name);
    }
}
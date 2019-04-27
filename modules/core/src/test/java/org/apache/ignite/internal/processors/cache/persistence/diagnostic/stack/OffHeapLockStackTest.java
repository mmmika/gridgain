package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.PageLockStackTest;

public class OffHeapLockStackTest extends PageLockStackTest {
    @Override protected LockStack createLockStackTracer(String name) {
        return new OffHeapLockStack("Thread=" + name);
    }
}

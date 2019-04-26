package org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractLockStackTest;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.LocksStackSnapshot;

public class OffHeapLockStackTest extends AbstractLockStackTest {
    @Override protected AbstractPageLockTracker<LocksStackSnapshot> createLockStackTracer(String name) {
        return new OffHeapLockStack(name);
    }
}

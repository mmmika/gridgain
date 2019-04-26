package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack.OffHeapLockStack;

public class OffHeapLockStackTest extends AbstractLockStackTest {
    @Override protected AbstractPageLockTracker<LocksStackSnapshot> createLockStackTracer(String name) {
        return new OffHeapLockStack(name);
    }
}

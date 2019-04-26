package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.stack.HeapArrayLockStack;

public class HeapArrayLockStackTest extends AbstractLockStackTest {
    @Override protected AbstractPageLockTracker<LocksStackSnapshot> createLockTracer(String name) {
        return new HeapArrayLockStack(name);
    }
}

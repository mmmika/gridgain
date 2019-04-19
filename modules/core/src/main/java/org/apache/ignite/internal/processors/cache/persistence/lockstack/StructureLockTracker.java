package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;

public class StructureLockTracker implements PageLockListener {

    private final String structureName;

    private final Map<Long, LockStack> threadStacks = new ConcurrentHashMap<>();

    /** */
    private final ThreadLocal<LockStack> lockTracker = ThreadLocal.withInitial(() -> {
        Thread thread = Thread.currentThread();

        String threadName = thread.getName();
        long threadId = thread.getId();

        LockStack stack = createLockStack(threadName + " - " + name(), threadId);

        threadStacks.put(threadId, stack);

        return stack;
    });

    private StructureLockTracker(String structureName) {
        this.structureName = structureName;
    }

    public static StructureLockTracker createTracker(String structureName) {
        return new StructureLockTracker(structureName);
    }

    public String name() {
        return structureName;
    }

    @Override public void onBeforeWriteLock(int cacheId, long pageId, long page) {
        // No-op.
    }

    @Override public void onWriteLock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().push(cacheId, pageId, LockStack.WRITE);
    }

    @Override public void onWriteUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().pop(cacheId, pageId, LockStack.WRITE);
    }

    @Override public void onBeforeReadLock(int cacheId, long pageId, long page) {
        // No-op.
    }

    @Override public void onReadLock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().push(cacheId, pageId, LockStack.READ);
    }

    @Override public void onReadUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().pop(cacheId, pageId, LockStack.READ);
    }

    private LockStack createLockStack(String name, long threadId) {
        //return new OffHeapLockStack(name, threadId);
        return new HeapArrayLockStack(name, threadId);
    }
}

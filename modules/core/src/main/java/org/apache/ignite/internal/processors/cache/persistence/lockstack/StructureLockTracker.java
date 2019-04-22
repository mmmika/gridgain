package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;

public class StructureLockTracker implements PageLockListener {

    private final String structureName;

    private final Map<Long, LockLog> threadStacks = new ConcurrentHashMap<>();

    /** */
    private final ThreadLocal<LockLog> lockTracker = ThreadLocal.withInitial(() -> {
        Thread thread = Thread.currentThread();

        String threadName = thread.getName();
        long threadId = thread.getId();

        LockLog stack = createLockStack(threadName + " - " + name(), threadId);

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
        lockTracker.get().readLock(cacheId, pageId);
    }

    @Override public void onWriteUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().readUnlock(cacheId, pageId);
    }

    @Override public void onBeforeReadLock(int cacheId, long pageId, long page) {
        // No-op.
    }

    @Override public void onReadLock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().readLock(cacheId, pageId);
    }

    @Override public void onReadUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().readUnlock(cacheId, pageId);
    }

    private LockLog createLockStack(String name, long threadId) {
        //return new OffHeapLockLog(name, threadId);
        return new HeapArrayLockLog(name, threadId);
    }
}

package org.apache.ignite.internal.processors.cache.persistence.lockdiagnostic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;

public class DataStructurePageLockTracker implements PageLockListener {

    private final String structureName;

    private final Map<Long, LockInterceptor> threadStacks = new ConcurrentHashMap<>();

    /** */
    private final ThreadLocal<LockInterceptor> lockTracker = ThreadLocal.withInitial(() -> {
        Thread thread = Thread.currentThread();

        String threadName = thread.getName();
        long threadId = thread.getId();

        LockInterceptor stack = createLockStack(threadName + "[" + threadId + "]" + " - " + name());

        threadStacks.put(threadId, stack);

        return stack;
    });

    private DataStructurePageLockTracker(String structureName) {
        this.structureName = structureName;
    }

    public static DataStructurePageLockTracker createTracker(String structureName) {
        return new DataStructurePageLockTracker(structureName);
    }

    public String name() {
        return structureName;
    }

    @Override public void onBeforeWriteLock(int cacheId, long pageId, long page) {
        lockTracker.get().beforeWriteLock(cacheId, pageId);
    }

    @Override public void onWriteLock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().writeLock(cacheId, pageId);
    }

    @Override public void onWriteUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().writeUnLock(cacheId, pageId);
    }

    @Override public void onBeforeReadLock(int cacheId, long pageId, long page) {
        lockTracker.get().beforeReadLock(cacheId, pageId);
    }

    @Override public void onReadLock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().readLock(cacheId, pageId);
    }

    @Override public void onReadUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().readUnlock(cacheId, pageId);
    }

    private LockInterceptor createLockStack(String name) {
        //return new OffHeapLockInterceptor(name, threadId);
        //return new HeapArrayLockLog(name, threadId);
        return new HeapArrayLockStack(name);
    }
}

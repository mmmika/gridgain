package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;

public class DataStructurePageLockTracker implements PageLockListener {

    private final String structureName;

    private final Map<Long, PageLockListener> threadStacks = new ConcurrentHashMap<>();

    /** */
    private final ThreadLocal<PageLockListener> lockTracker = ThreadLocal.withInitial(() -> {
        Thread thread = Thread.currentThread();

        String threadName = thread.getName();
        long threadId = thread.getId();

        PageLockListener stack = createLockStack(threadName + "[" + threadId + "]" + " - " + name());

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
        lockTracker.get().onBeforeWriteLock(cacheId, pageId, page);
    }

    @Override public void onWriteLock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().onWriteLock(cacheId, pageId, page, pageAddr);
    }

    @Override public void onWriteUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().onWriteUnlock(cacheId, pageId, page, pageAddr);
    }

    @Override public void onBeforeReadLock(int cacheId, long pageId, long page) {
        lockTracker.get().onBeforeReadLock(cacheId, pageId, page);
    }

    @Override public void onReadLock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().onReadLock(cacheId, pageId, page, pageAddr);
    }

    @Override public void onReadUnlock(int cacheId, long pageId, long page, long pageAddr) {
        lockTracker.get().onReadUnlock(cacheId, pageId, page, pageAddr);
    }

    private PageLockListener createLockStack(String name) {
        //return new OffHeapLockStack(name, threadId);
        //return new HeapArrayLockLog(name, threadId);
        return new HeapArrayLockStack(name);
    }
}

package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

public class OffHeapLockLog extends AbstractPageLockTracker {

    protected OffHeapLockLog(String name) {
        super(name);
    }

    @Override protected void onBeforeWriteLock0(int cacheId, long pageId, long page) {

    }

    @Override protected void onWriteLock0(int cacheId, long pageId, long page, long pageAddr) {

    }

    @Override protected void onWriteUnlock0(int cacheId, long pageId, long page, long pageAddr) {

    }

    @Override protected void onBeforeReadLock0(int cacheId, long pageId, long page) {

    }

    @Override protected void onReadLock0(int cacheId, long pageId, long page, long pageAddr) {

    }

    @Override protected void onReadUnlock0(int cacheId, long pageId, long page, long pageAddr) {

    }

    @Override public Dump dump() {
        return null;
    }
}

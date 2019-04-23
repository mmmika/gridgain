package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;

public class OffHeapLockStack implements PageLockListener {

    @Override public void onBeforeWriteLock(int cacheId, long pageId, long page) {

    }

    @Override public void onWriteLock(int cacheId, long pageId, long page, long pageAddr) {

    }

    @Override public void onWriteUnlock(int cacheId, long pageId, long page, long pageAddr) {

    }

    @Override public void onBeforeReadLock(int cacheId, long pageId, long page) {

    }

    @Override public void onReadLock(int cacheId, long pageId, long page, long pageAddr) {

    }

    @Override public void onReadUnlock(int cacheId, long pageId, long page, long pageAddr) {

    }
}

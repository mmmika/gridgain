package org.apache.ignite.internal.processors.cache.persistence.lockdiagnostic;

import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.util.GridUnsafe.getLong;

public class OffHeapLockInterceptor implements LockInterceptor {
    @Override public void beforeReadLock(int cacheId, long pageId) {

    }

    @Override public void readLock(int cacheId, long pageId) {

    }

    @Override public void readUnlock(int cacheId, long pageId) {

    }

    @Override public void beforeWriteLock(int cacheId, long pageId) {

    }

    @Override public void writeLock(int cacheId, long pageId) {

    }

    @Override public void writeUnLock(int cacheId, long pageId) {

    }
}

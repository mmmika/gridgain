package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import org.apache.ignite.IgniteException;

public interface LockInterceptor {
    void beforeReadLock(int cacheId, long pageId);

    void readLock(int cacheId, long pageId);

    void readUnlock(int cacheId, long pageId);

    void beforeWriteLock(int cacheId, long pageId);

    void writeLock(int cacheId, long pageId);

    void writeUnLock(int cacheId, long pageId);
}

package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import org.apache.ignite.IgniteException;

public interface LockLog {
    void readLock(int cacheId, long pageId);

    void readUnlock(int cacheId, long pageId);

    void writeLock(int cacheId, long pageId);

    void writeUnLock(int cacheId, long pageId);

    int poistionIdx();

    int capacity();
}

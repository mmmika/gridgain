package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import org.apache.ignite.IgniteException;

public interface LockStack {
    void push(int cacheId, long pageId, int flags);

    void pop(int cacheId, long pageId, int flags);

    int poistionIdx();

    int capacity();

    int READ = 0b0000_0000_0000_0001;
    int WRITE = 0b0000_0000_0000_0010;
}

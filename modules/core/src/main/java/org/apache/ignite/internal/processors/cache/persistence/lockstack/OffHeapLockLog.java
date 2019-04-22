package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.NoSuchElementException;
import org.apache.ignite.internal.util.GridUnsafe;
import org.apache.ignite.internal.util.typedef.internal.SB;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.GridUnsafe.getLong;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class OffHeapLockLog implements LockLog {
    @Override public void readLock(int cacheId, long pageId) {

    }

    @Override public void readUnlock(int cacheId, long pageId) {

    }

    @Override public void writeLock(int cacheId, long pageId) {

    }

    @Override public void writeUnLock(int cacheId, long pageId) {

    }

    @Override public int poistionIdx() {
        return 0;
    }

    @Override public int capacity() {
        return 0;
    }
}

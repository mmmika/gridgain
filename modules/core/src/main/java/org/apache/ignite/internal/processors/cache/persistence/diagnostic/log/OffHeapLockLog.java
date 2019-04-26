package org.apache.ignite.internal.processors.cache.persistence.diagnostic.log;

import org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.Dump;

public class OffHeapLockLog extends AbstractPageLockTracker {

    protected OffHeapLockLog(String name) {
        super(name);
    }

    @Override protected void onBeforeWriteLock0(int structureId, long pageId, long page) {

    }

    @Override protected void onWriteLock0(int structureId, long pageId, long page, long pageAddr) {

    }

    @Override protected void onWriteUnlock0(int structureId, long pageId, long page, long pageAddr) {

    }

    @Override protected void onBeforeReadLock0(int structureId, long pageId, long page) {

    }

    @Override protected void onReadLock0(int structureId, long pageId, long page, long pageAddr) {

    }

    @Override protected void onReadUnlock0(int structureId, long pageId, long page, long pageAddr) {

    }

    @Override protected Dump dump0() {
        return null;
    }
}

package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.apache.ignite.lang.IgniteFuture;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public abstract class AbstractPageLockTracker<T> implements DumpSupported {
    protected static final int READ_LOCK = 1;
    protected static final int READ_UNLOCK = 2;
    protected static final int WRITE_LOCK = 3;
    protected static final int WRITE_UNLOCK = 4;
    protected static final int BEFORE_READ_LOCK = 5;
    protected static final int BEFORE_WRITE_LOCK = 6;

    protected final String name;

    protected int headIdx;

    private volatile boolean dump;

    private volatile boolean locked;

    protected AbstractPageLockTracker(String name) {
        this.name = name;
    }

    protected void lock() {
        while (!lock0()) {
            // Busy wait.
        }
    }

    private boolean lock0() {
        awaitDump();

        locked = true;
        if (dump) {
            locked = false;

            return false;
        }

        return true;
    }

    protected void unLock() {
        locked = false;
    }

    protected void awaitDump() {
        while (dump) {
            // Busy wait.
        }
    }

    protected void awaitLocks() {
        while (locked) {
            // Busy wait.
        }
    }

    protected void prepareDump() {
        dump = true;
    }

    protected void onDumpComplete() {
        dump = false;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        Dump dump = dump();

        return dump.toString();
    }


    /** {@inheritDoc} */
    @Override public IgniteFuture dumpSync() {
        //TODO
        throw new UnsupportedOperationException();
    }

    protected static String pageIdToString(long pageId) {
        return "pageId=" + pageId
            + " [pageIdxHex=" + hexLong(pageId)
            + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
            + ", flags=" + hexInt(flag(pageId)) + "]";
    }
}

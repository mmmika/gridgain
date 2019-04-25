package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;
import org.apache.ignite.internal.util.typedef.internal.SB;

import static java.util.Arrays.copyOf;
import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockLog
    extends AbstractPageLockTracker<HeapArrayLockLog.LocksState>
    implements PageLockListener {
    private static final int OP_OFFSET = 16;
    private static final int LOCK_IDX_MASK = 0xFFFF0000;
    private static final int LOCK_OP_MASK = 0x000000000000FF;

    protected int headIdx;

    private final long[] pageIdsLockLog = new long[1024 * 2];

    private int holdedLockCnt;

    private int nextOpCacheId;
    private long nextOpPageId;
    private int nextOp;

    public HeapArrayLockLog(String name) {
        super("name=" + name);
    }

    @Override protected void onBeforeWriteLock0(int cacheId, long pageId, long page) {
        this.nextOpCacheId = cacheId;
        this.nextOpPageId = pageId;
        this.nextOp = BEFORE_WRITE_LOCK;
    }

    @Override protected void onWriteLock0(int cacheId, long pageId, long page, long pageAddr) {
        log(cacheId, pageId, WRITE_LOCK);
    }

    @Override protected void onWriteUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        log(cacheId, pageId, WRITE_UNLOCK);
    }

    @Override protected void onBeforeReadLock0(int cacheId, long pageId, long page) {
        this.nextOpCacheId = cacheId;
        this.nextOpPageId = pageId;
        this.nextOp = BEFORE_READ_LOCK;
    }

    @Override protected void onReadLock0(int cacheId, long pageId, long page, long pageAddr) {
        log(cacheId, pageId, READ_LOCK);
    }

    @Override public void onReadUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        log(cacheId, pageId, READ_UNLOCK);
    }

    private void log(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        if (headIdx + 2 > pageIdsLockLog.length)
            throw new StackOverflowError("Stack overflow, size:" + pageIdsLockLog.length +
                "\n nextOpCacheId=" + cacheId + ", nextOpPageId=" + pageId + ", flags=" + flags +
                "\n" + toString());

        long pageId0 = pageIdsLockLog[headIdx];

        assert pageId0 == 0L || pageId0 == pageId :
            "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", nextOpPageId=" + pageId;

        pageIdsLockLog[headIdx] = pageId;

        if (READ_LOCK == flags || WRITE_LOCK == flags)
            holdedLockCnt++;

        if (READ_UNLOCK == flags || WRITE_UNLOCK == flags)
            holdedLockCnt--;

        int curIdx = holdedLockCnt << OP_OFFSET & LOCK_IDX_MASK;

        long meta = meta(cacheId, curIdx | flags);

        pageIdsLockLog[headIdx + 1] = meta;

        if (BEFORE_READ_LOCK == flags || BEFORE_WRITE_LOCK == flags)
            return;

        headIdx += 2;

        if (holdedLockCnt == 0)
            reset();

        if (this.nextOpPageId == pageId && this.nextOpCacheId == cacheId) {
            this.nextOpCacheId = 0;
            this.nextOpPageId = 0;
            this.nextOp = 0;
        }
    }

    private void reset() {
        for (int i = 0; i < headIdx; i++)
            pageIdsLockLog[i] = 0;

        headIdx = 0;
    }

    private long meta(int cacheId, int flags) {
        long major = ((long)flags) << 32;

        long minor = (long)cacheId;

        return major | minor;
    }

    @Override public LocksState dump() {
        prepareDump();

        awaitLocks();

        long[] pageIdsLockLog = copyOf(this.pageIdsLockLog, this.pageIdsLockLog.length);

        LocksState locksState = new LocksState(this.name + " (time=" + System.currentTimeMillis() + ")", pageIdsLockLog);
        locksState.headIdx = headIdx;
        locksState.nextOp = nextOp;
        locksState.nextOpCacheId = nextOpCacheId;
        locksState.nextOpPageId = nextOpPageId;

        onDumpComplete();

        return locksState;
    }

    private static class LockState {
        int readlock;
        int writelock;
    }

    public static class LocksState implements Dump {
        private final String name;

        private int headIdx;

        private final long[] pageIdsLockLog;

        private int nextOpCacheId;
        private long nextOpPageId;
        private int nextOp;

        public LocksState(String name, long[] pageIdsLockLog) {
            this.name = name;
            this.pageIdsLockLog = pageIdsLockLog;
        }

        @Override public String toString() {
            SB res = new SB();

            res.a(name).a("\n");

            Map<Long, LockState> holdetLocks = new LinkedHashMap<>();

            SB logLocksStr = new SB();

            for (int i = 0; i < headIdx; i += 2) {
                long metaOnLock = pageIdsLockLog[i + 1];

                assert metaOnLock != 0;

                int idx = ((int)(metaOnLock >> 32) & LOCK_IDX_MASK) >> OP_OFFSET;

                assert idx >= 0;

                long pageId = pageIdsLockLog[i];

                int op = (int)((metaOnLock >> 32) & LOCK_OP_MASK);
                int cacheId = (int)(metaOnLock);

                String opStr = "N/A";

                switch (op) {
                    case READ_LOCK:
                        opStr = "Read lock    ";
                        break;
                    case READ_UNLOCK:
                        opStr = "Read unlock  ";
                        break;
                    case WRITE_LOCK:
                        opStr = "Write lock    ";
                        break;
                    case WRITE_UNLOCK:
                        opStr = "Write unlock  ";
                        break;
                }

                if (op == READ_LOCK || op == WRITE_LOCK || op == BEFORE_READ_LOCK) {
                    LockState state = holdetLocks.get(pageId);

                    if (state == null)
                        holdetLocks.put(pageId, state = new LockState());

                    if (op == READ_LOCK)
                        state.readlock++;

                    if (op == WRITE_LOCK)
                        state.writelock++;

                    logLocksStr.a("L=" + idx + " -> " + opStr + " nextOpPageId=" + pageId + ", nextOpCacheId=" + cacheId
                        + " [pageIdxHex=" + hexLong(pageId)
                        + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
                        + ", flags=" + hexInt(flag(pageId)) + "]\n");
                }

                if (op == READ_UNLOCK || op == WRITE_UNLOCK) {
                    LockState state = holdetLocks.get(pageId);

                    if (op == READ_UNLOCK)
                        state.readlock--;

                    if (op == WRITE_UNLOCK)
                        state.writelock--;

                    if (state.readlock == 0 && state.writelock == 0)
                        holdetLocks.remove(pageId);

                    logLocksStr.a("L=" + idx + " <- " + opStr + " nextOpPageId=" + pageId + ", nextOpCacheId=" + cacheId
                        + " [pageIdxHex=" + hexLong(pageId)
                        + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
                        + ", flags=" + hexInt(flag(pageId)) + "]\n");
                }
            }

            if (nextOpPageId != 0) {
                String opStr = "N/A";

                switch (nextOp) {
                    case BEFORE_READ_LOCK:
                        opStr = "Try read lock    ";
                        break;
                    case BEFORE_WRITE_LOCK:
                        opStr = "Try write lock  ";
                        break;
                }

                logLocksStr.a("-> " + opStr + " nextOpPageId=" + nextOpPageId + ", nextOpCacheId=" + nextOpCacheId
                    + " [pageIdxHex=" + hexLong(nextOpPageId)
                    + ", partId=" + pageId(nextOpPageId) + ", pageIdx=" + pageIndex(nextOpPageId)
                    + ", flags=" + hexInt(flag(nextOpPageId)) + "]\n");
            }

            SB holdetLocksStr = new SB();

            holdetLocksStr.a("locked pages = [");

            boolean first = true;

            for (Map.Entry<Long, LockState> entry : holdetLocks.entrySet()) {
                Long pageId = entry.getKey();
                LockState lockState = entry.getValue();

                if (!first)
                    holdetLocksStr.a(",");
                else
                    first = false;

                holdetLocksStr.a(pageId).a("(r=" + lockState.readlock + "|w=" + lockState.writelock + ")");
            }

            holdetLocksStr.a("]\n");

            res.a(holdetLocksStr);
            res.a(logLocksStr);

            return res.toString();
        }
    }
}

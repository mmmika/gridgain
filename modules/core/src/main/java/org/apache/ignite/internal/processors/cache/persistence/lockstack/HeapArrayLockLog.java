package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ignite.internal.util.typedef.internal.SB;
import org.apache.ignite.internal.util.typedef.internal.U;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockLog implements LockLog {
    private int headIdx;
    private int holdedLockCnt;

    private static final int OP_OFFSET = 16;
    private static final int LOCK_IDX_MASK = 0xFFFF0000;
    private static final int LOCK_OP_MASK = 0b0000_0000_0000_0011;

    private static final int READ_LOCK = 1;
    private static final int READ_UNLOCK = 2;
    private static final int WRITE_LOCK = 3;
    private static final int WRITE_UNLOCK = 4;
    private static final int BEFORE_READ_LOCK = 5;
    private static final int BEFORE_WRITE_LOCK = 6;

    private final long[] arrPageIds = new long[1024 * 2];

    private final String name;


    public HeapArrayLockLog(String name, long threadId) {
        this.name = "[name=" + name + ", thread=" + threadId + "]";
    }

    @Override public void readLock(int cacheId, long pageId) {
        log(cacheId, pageId, READ_LOCK);
    }

    @Override public void readUnlock(int cacheId, long pageId) {
        log(cacheId, pageId, READ_UNLOCK);
    }

    @Override public void writeLock(int cacheId, long pageId) {
        log(cacheId, pageId, WRITE_LOCK);
    }

    @Override public void writeUnLock(int cacheId, long pageId) {
        log(cacheId, pageId, WRITE_UNLOCK);
    }

    private void log(int cacheId, long pageId, int flags){
        assert pageId > 0;

        if (headIdx + 1 > arrPageIds.length)
            throw new StackOverflowError("Stack overflow, size:" + arrPageIds.length +
                "\n cacheId=" + cacheId + ", pageId=" + pageId + ", flags=" + flags +
                "\n" + toString());

        long pageId0 = arrPageIds[headIdx];

        assert pageId0 == 0L || pageId0 == pageId :
            "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        arrPageIds[headIdx] = pageId;

        if (READ_LOCK == flags || WRITE_LOCK == flags)
            holdedLockCnt++;

        if (READ_UNLOCK == flags || WRITE_UNLOCK == flags)
            holdedLockCnt--;

        int curIdx = holdedLockCnt << OP_OFFSET & LOCK_IDX_MASK;

        long meta = meta(cacheId, curIdx | flags);

        arrPageIds[headIdx + 1] = meta;

        if (BEFORE_READ_LOCK == flags || BEFORE_WRITE_LOCK == flags)
            return;

        headIdx += 2;

        if (holdedLockCnt == 0)
            reset();
    }

    private void reset() {
        for (int i = 0; i < headIdx; i++)
            arrPageIds[i] = 0;

        headIdx = 0;
    }

    @Override public int poistionIdx() {
        return headIdx / 2;
    }

    @Override public int capacity() {
        return arrPageIds.length / 2;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        SB res = new SB();

        res.a(name).a("\n");

        if (headIdx == 0)
            return res.a("[Empty]").toString();

        Map<Long, LockState> holdetLocks = new LinkedHashMap<>();

        SB logLocksStr = new SB();

        for (int i = 0; i < headIdx; i +=2) {
            long metaOnLock = arrPageIds[i + 1];

            assert metaOnLock != 0;

            int idx = ((int)(metaOnLock >> 32) & LOCK_IDX_MASK) >> OP_OFFSET;

            assert idx >= 0;

            long pageId = arrPageIds[i];

            int op = (int)((metaOnLock >> 32) & LOCK_OP_MASK);
            int cacheId = (int)(metaOnLock);

            String opStr = "N/A";

            switch (op) {
                case READ_LOCK:
                    opStr = "Read lock  ";
                    break;
                case READ_UNLOCK:
                    opStr = "Read unlock";
                    break;
                case BEFORE_READ_LOCK:
                    opStr = "Try read lock";
                    break;
                case WRITE_LOCK:
                    opStr = "Write lock  ";
                    break;
                case WRITE_UNLOCK:
                    opStr = "Write unlock";
                    break;
                case BEFORE_WRITE_LOCK:
                    opStr = "Try write unlock";
                    break;
            }

            if (op == READ_LOCK || op == WRITE_LOCK) {
                LockState state = holdetLocks.get(pageId);

                if (state == null)
                    holdetLocks.put(pageId, state = new LockState());

                if (op == READ_LOCK)
                    state.readlock++;

                if (op == WRITE_LOCK)
                    state.writelock++;

                logLocksStr.a("L=" + idx + " -> " + opStr + " pageId=" + pageId + ", cacheId=" + cacheId
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

                logLocksStr.a("L=" + idx + " <- " + opStr + " pageId=" + pageId + ", cacheId=" + cacheId
                    + " [pageIdxHex=" + hexLong(pageId)
                    + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
                    + ", flags=" + hexInt(flag(pageId)) + "]\n");
            }
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

    private long meta(int cacheId, int flags) {
        long major = ((long)flags) << 32;

        long minor = (long)cacheId;

        return major | minor;
    }

    private static class LockState {
        int readlock;
        int writelock;
    }
}

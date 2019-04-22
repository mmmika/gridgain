package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import org.apache.ignite.internal.util.typedef.internal.SB;
import org.apache.ignite.internal.util.typedef.internal.U;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockLog implements LockLog {
    private int headIdx;
    private int maxHeadIdx;
    private int holdedLockCnt;

    private static final int OP_OFFSET = 16;
    private static final int LOCK_IDX_MASK = 0xFFFF0000;
    private static final int LOCK_OP_MASK = 0b0000_0000_0000_0011;

    private static final int READ_LOCK = 1;
    private static final int READ_UNLOCK = 2;
    private static final int WRITE_LOCK = 3;
    private static final int WRITE_UNLOCK = 4;

    private final long[] arrPageIds = new long[1024 * 2];

    private final String name;

    public static void main(String[] args) {
        System.out.println(U.hexInt(LOCK_IDX_MASK));
        System.out.println(U.hexInt(LOCK_OP_MASK));
    }

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
            throw new StackOverflowError("Stack overflow, size:" + arrPageIds.length);

        long pageId0 = arrPageIds[headIdx];

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        arrPageIds[headIdx] = pageId;

        if (READ_LOCK == flags || WRITE_LOCK == flags)
            holdedLockCnt++;

        if (READ_UNLOCK == flags || WRITE_UNLOCK == flags)
            holdedLockCnt--;

        int curIdx = holdedLockCnt << OP_OFFSET & LOCK_IDX_MASK;

        long meta = meta(cacheId, curIdx | flags);

        arrPageIds[headIdx + 1] = meta;

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
        SB sb = new SB();

        sb.a(name).a("\n");

        for (int i = 0; i < headIdx; i +=2) {
            SB tab = new SB();

            long metaOnLock = arrPageIds[i + 1];

            assert metaOnLock != 0;

            int idx = ((int)(metaOnLock >> 32) & LOCK_IDX_MASK) >> OP_OFFSET;

            assert idx >= 0;

            for (int j = 1; j < idx; j ++)
                tab.a("\t");

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
                case WRITE_LOCK:
                    opStr = "Write lock  ";
                    break;
                case WRITE_UNLOCK:
                    opStr = "Write unlock";
                    break;
            }

            if (op == READ_LOCK || op == WRITE_LOCK) {
                sb.a(tab + "L=" + idx + " -> " + opStr + " pageId=" + pageId + ", cacheId=" + cacheId
                    + " [pageIdxHex=" + hexLong(pageId)
                    + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
                    + ", flags=" + hexInt(flag(pageId)) + "]\n");
            }

            if (op == READ_UNLOCK || op == WRITE_UNLOCK) {
                sb.a(tab + "L=" + idx + " <- " + opStr + " pageId=" + pageId + ", cacheId=" + cacheId
                    + " [pageIdxHex=" + hexLong(pageId)
                    + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
                    + ", flags=" + hexInt(flag(pageId)) + "]\n");

            }
        }

        return sb.toString();
    }

    private long meta(int cacheId, int flags) {
        long major = ((long)flags) << 32;

        long minor = (long)cacheId;

        return major | minor;
    }
}

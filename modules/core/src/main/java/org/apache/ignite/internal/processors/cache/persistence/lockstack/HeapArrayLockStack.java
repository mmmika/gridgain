package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.NoSuchElementException;
import org.apache.ignite.internal.util.typedef.internal.SB;
import org.apache.ignite.internal.util.typedef.internal.U;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockStack implements LockStack {
    private int headIdx;
    private int maxHeadIdx;

    private static final int OP_OFFSET = 16;
    private static final int LOCK_IDX_MASK = 0xFFFF0000;
    private static final int LOCK_OP_MASK = 0b0000_0000_0000_0011;

    private final long[] arrPageIds = new long[64];
    private final long[] arrMeta = new long[64];

    private final String name;

    public static void main(String[] args) {
        System.out.println(U.hexInt(LOCK_IDX_MASK));
        System.out.println(U.hexInt(LOCK_OP_MASK));
    }

    public HeapArrayLockStack(String name, long threadId) {
        this.name = "name=" + name + " thread=" + threadId;
    }

    @Override public void push(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        if (headIdx + 2 > arrPageIds.length)
            throw new StackOverflowError("Stack overflow, size:" + arrPageIds.length);

        long pageId0 = arrPageIds[headIdx];

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        arrPageIds[headIdx] = pageId;
        arrMeta[headIdx] = meta(cacheId,
            (headIdx << OP_OFFSET & LOCK_IDX_MASK) | flags);

        headIdx += 2;
        maxHeadIdx += 2;
    }

    private long meta(int cacheId, int flags) {
        long major = ((long)flags) << 32;
        long minor = (long)cacheId;
        return  major | minor;
    }

    @Override public void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        if (headIdx > 2) {
            int last = headIdx - 2;

            long val = arrPageIds[last];

            if (val == pageId) {
                arrPageIds[last + 1] = pageId;
                arrMeta[last + 1] = meta(cacheId,
                    (headIdx << OP_OFFSET & LOCK_IDX_MASK) | flags);

                headIdx -= 2;
            }
            else {
                for (int i = last - 2; i >= 0; i--) {
                    if (arrPageIds[i] == pageId) {
                        arrPageIds[i + 1] = pageId;
                        arrMeta[i + 1] = meta(cacheId,
                            (headIdx << OP_OFFSET & LOCK_IDX_MASK) | flags);

                        return;
                    }
                }

                assert false : "Never should happened since with obtaine lock (push to stack) before unlock.\n"
                    + toString();
            }
        }
        else {
            long val = arrPageIds[0];

            if (val == 0)
                throw new NoSuchElementException(
                    "Stack is empty, can not pop elemnt with cacheId=" +
                        cacheId + ", pageId=" + pageId + ", flags=" + hexInt(flags));

            if (val == pageId) {
                for (int i = 0; i < maxHeadIdx; i++) {
                    arrPageIds[i] = 0;
                    arrMeta[i] = 0;
                }

                headIdx = 0;
                maxHeadIdx = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }

    @Override public int poistionIdx() {
        return headIdx / 2;
    }

    @Override public int capacity() {
        return arrPageIds.length;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        SB sb = new SB();

        sb.a(name).a("\n");

        for (int i = 0; i < maxHeadIdx; i += 2) {
            SB tab = new SB();

            long metaOnLock = arrMeta[i + 1];

            assert metaOnLock != 0;

            int idx = ((int)(metaOnLock >> 32) & LOCK_IDX_MASK) >> OP_OFFSET;

            assert idx >= 0;

            for (int j = -1; j < idx; j += 2)
                tab.a("\t");

            long pageIdOnLock = arrPageIds[i];

            int op = (int)((metaOnLock >> 32) & LOCK_OP_MASK);
            int cacheId = (int)(metaOnLock);

            String opStr = op == LockStack.READ ? "Read lock" : (op == LockStack.WRITE ? "Write lock" : "N/A");

            sb.a(tab + "L=" + idx + " -> " + opStr + " pageId=" + pageIdOnLock + ", cacheId=" + cacheId
                + " [pageIdxHex=" + hexLong(pageIdOnLock)
                + ", partId=" + pageId(pageIdOnLock) + ", pageIdx=" + pageIndex(pageIdOnLock)
                + ", flags=" + hexInt(flag(pageIdOnLock)) + "]\n");

        }

        for (int i = maxHeadIdx - 1; i > 0; i -= 2) {
            SB tab = new SB();

            long pageIdOnUnLock = arrPageIds[i];

            if (pageIdOnUnLock == 0)
                continue;

            long metaOnUnLock = arrMeta[i + 1];

            assert metaOnUnLock != 0;

            int idx = ((int)(metaOnUnLock >> 32) & LOCK_IDX_MASK) >> OP_OFFSET;

            for (int j = -1; j < idx; j += 2)
                tab.a("\t");

            int op = (int)((metaOnUnLock >> 32) & LOCK_OP_MASK);
            int cacheId = (int)(metaOnUnLock);

            String opStr = op == LockStack.READ ? "Read lock" : (op == LockStack.WRITE ? "Write lock" : "N/A");

            sb.a(tab + "L=" + idx + " <- " + opStr + " pageId=" + pageIdOnUnLock + ", cacheId=" + cacheId
                + " [pageIdxHex=" + hexLong(pageIdOnUnLock)
                + ", partId=" + pageId(pageIdOnUnLock) + ", pageIdx=" + pageIndex(pageIdOnUnLock)
                + ", flags=" + hexInt(flag(pageIdOnUnLock)) + "]\n");
        }

        return sb.toString();
    }
}

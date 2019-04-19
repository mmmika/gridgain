package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.NoSuchElementException;
import org.apache.ignite.internal.util.typedef.internal.SB;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockStack implements LockStack {
    private int headIdx;

    private static final int OP_OFFSET = 16;
    private static final int LOCK_OP_MASK = 0b1;
    private static final int LOCK_OP_TYPE_MASK = 0b10;

    private final long[] arrPageIds = new long[64];
    private final long[] arrMeta = new long[64];

    private final String name;

    public HeapArrayLockStack(String name, long threadId) {
        this.name = name + " - " + threadId;
    }

    @Override public void push(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        if (headIdx + 1 > arrPageIds.length)
            throw new StackOverflowError("Stack overflow, size:" + arrPageIds.length);

        long pageId0 = arrPageIds[headIdx];

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        arrPageIds[headIdx] = pageId;
        arrMeta[headIdx] = meta(cacheId, flags);

        headIdx += 2;
    }

    private long meta(int cacheId, int flags) {
        return (((long)(flags & OP_OFFSET) << 32)) | ((long)cacheId);
    }

    @Override public void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        if (headIdx > 2) {
            int last = headIdx - 2;

            long val = arrPageIds[last];

            if (val == pageId) {
                arrPageIds[last + 1] = pageId;
                arrMeta[last + 1] = meta(cacheId, flags);

                headIdx -= 2;
            }
            else {
                for (int i = last - 2; i >= 0; i--) {
                    if (arrPageIds[i] == pageId) {
                        arrPageIds[i] = pageId;
                        arrMeta[i] = meta(cacheId, flags);

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
                arrPageIds[0] = 0;
                arrMeta[0] = 0;

                headIdx = 0;
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

        for (int i = 0; i < capacity(); i += 2) {
            SB tab = new SB();

            for (int j = -1; j < i; j += 2)
                tab.a("\t");

            long pageIdOnLock = arrPageIds[i];
            long pageIdOnUnLock = arrPageIds[i + 1];

            if (pageIdOnLock == 0) {
                sb.a("L=" + i + "-> [empty]\n" + tab);

                continue;
            }

            long metaOnLock = arrMeta[i];
            long metaOnUnLock = arrMeta[i + 1];

            int op = (int)((metaOnLock >> 32) & OP_OFFSET);
            int cacheId = (int)(metaOnLock);

            String opStr = op == LockStack.READ ? "Read lock" : (op == LockStack.WRITE ? "Write lock" : "N/A");

            sb.a(tab + "L=" + (i / 2) + " -> " + opStr + " pageId=" + pageIdOnLock + ", cacheId=" + cacheId
                + " [pageIdxHex=" + hexLong(pageIdOnLock)
                + ", partId=" + pageId(pageIdOnLock) + ", pageIdx=" + pageIndex(pageIdOnLock)
                + ", flags=" + hexInt(flag(pageIdOnLock)) + "]\n");

            if (metaOnUnLock != 0) {
                sb.a(tab + "L=" + (i / 2) + " <- " + opStr + " pageId=" + pageIdOnUnLock + ", cacheId=" + cacheId
                    + " [pageIdxHex=" + hexLong(pageIdOnUnLock)
                    + ", partId=" + pageId(pageIdOnUnLock) + ", pageIdx=" + pageIndex(pageIdOnUnLock)
                    + ", flags=" + hexInt(flag(pageIdOnUnLock)) + "]\n");
            }
        }

        return sb.toString();
    }
}

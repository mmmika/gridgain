package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.NoSuchElementException;
import org.apache.ignite.internal.util.typedef.internal.SB;

import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockStack implements LockInterceptor {
    private int headIdx;

    private static final int READ_LOCK = 1;
    private static final int READ_UNLOCK = 2;
    private static final int WRITE_LOCK = 3;
    private static final int WRITE_UNLOCK = 4;
    private static final int BEFORE_READ_LOCK = 5;
    private static final int BEFORE_WRITE_LOCK = 6;

    private final long[] arrPageIds = new long[64];

    private final String name;

    private long nextPage;
    private int op;

    public HeapArrayLockStack(String name) {
        this.name = "name=" + name;
    }

    @Override public void beforeReadLock(int cacheId, long pageId) {
        nextPage = pageId;
        op = BEFORE_READ_LOCK;
    }

    @Override public void readLock(int cacheId, long pageId) {
        push(cacheId, pageId, READ_LOCK);
    }

    @Override public void readUnlock(int cacheId, long pageId) {
        pop(cacheId, pageId, READ_UNLOCK);
    }

    @Override public void beforeWriteLock(int cacheId, long pageId) {
        nextPage = pageId;
        op = BEFORE_WRITE_LOCK;
    }

    @Override public void writeLock(int cacheId, long pageId) {
        push(cacheId, pageId, WRITE_LOCK);
    }

    @Override public void writeUnLock(int cacheId, long pageId) {
        pop(cacheId, pageId, WRITE_UNLOCK);
    }

    private void push(int cacheId, long pageId, int flags) {
        reset();

        assert pageId > 0;

        if (headIdx + 1 > arrPageIds.length)
            throw new StackOverflowError("Stack overflow, size:" + arrPageIds.length);

        long pageId0 = arrPageIds[headIdx];

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        arrPageIds[headIdx] = pageId;

        headIdx++;
    }

    private void reset() {
        nextPage = 0;
        op = 0;
    }

    private void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        reset();

        if (headIdx > 1) {
            int last = headIdx - 1;

            long val = arrPageIds[last];

            if (val == pageId) {
                arrPageIds[headIdx] = 0;

                //Reset head to the first not empty element.
                do {
                    headIdx--;
                }
                while (arrPageIds[headIdx] == 0);
            }
            else {
                for (int i = last - 1; i >= 0; i--) {
                    if (arrPageIds[i] == pageId) {
                        arrPageIds[i] = 0;

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
                for (int i = 0; i < headIdx; i++)
                    arrPageIds[i] = 0;

                headIdx = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        SB res = new SB();

        res.a(name).a("\n");
        res.a("locked pages stack = {");

        if (nextPage != 0) {
            String str = "N/A";

            if (op == BEFORE_READ_LOCK)
                str = "obtain read lock";
            else if (op == BEFORE_WRITE_LOCK)
                str = "obtain write lock";

            res.a(">>> try " + str + ", " + pageIdToString(nextPage) + "\n");
        }

        for (int i = headIdx; i >= 0; i--) {
            long pageId = arrPageIds[i];

            if (pageId == 0) {
                res.a(i + " -\n");
            }
            else {
                res.a(i + " " + pageIdToString(pageId) + "\n");
            }
        }

        res.a("}\n");

        return res.toString();
    }

    private String pageIdToString(long pageId) {
        return "pageId=" + pageId
            + " [pageIdxHex=" + hexLong(pageId)
            + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
            + ", flags=" + hexInt(flag(pageId)) + "]";
    }
}

package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.NoSuchElementException;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;
import org.apache.ignite.internal.util.typedef.internal.SB;

import static java.util.Arrays.copyOf;
import static org.apache.ignite.internal.pagemem.PageIdUtils.flag;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageId;
import static org.apache.ignite.internal.pagemem.PageIdUtils.pageIndex;
import static org.apache.ignite.internal.util.IgniteUtils.hexInt;
import static org.apache.ignite.internal.util.IgniteUtils.hexLong;

public class HeapArrayLockStack
    extends AbstractPageLockTracker<HeapArrayLockStack.LocksStateSnapshot> {
    private static final int STACK_SIZE = 128;

    protected int headIdx;

    private final long[] pageIdLocksStack;

    private long nextOpPageId;
    private int nextOp;

    public HeapArrayLockStack(String name) {
        super("name=" + name);
        this.pageIdLocksStack = new long[STACK_SIZE];
    }

    @Override public void onBeforeWriteLock0(int cacheId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_WRITE_LOCK;
    }

    @Override public void onWriteLock0(int cacheId, long pageId, long page, long pageAddr) {
        push(cacheId, pageId, WRITE_LOCK);
    }

    @Override public void onWriteUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        pop(cacheId, pageId, WRITE_UNLOCK);
    }

    @Override public void onBeforeReadLock0(int cacheId, long pageId, long page) {
        nextOpPageId = pageId;
        nextOp = BEFORE_READ_LOCK;
    }

    @Override public void onReadLock0(int cacheId, long pageId, long page, long pageAddr) {
        push(cacheId, pageId, READ_LOCK);
    }

    @Override public void onReadUnlock0(int cacheId, long pageId, long page, long pageAddr) {
        pop(cacheId, pageId, READ_UNLOCK);
    }

    private void push(int cacheId, long pageId, int flags) {
        reset();

        assert pageId > 0;

        if (headIdx + 1 > pageIdLocksStack.length)
            throw new StackOverflowError("Stack overflow, size:" + pageIdLocksStack.length);

        long pageId0 = pageIdLocksStack[headIdx];

        assert pageId0 == 0L : "Head should be empty, headIdx=" + headIdx + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        pageIdLocksStack[headIdx] = pageId;

        headIdx++;
    }

    private void reset() {
        nextOpPageId = 0;
        nextOp = 0;
    }

    private void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        reset();

        if (headIdx > 1) {
            int last = headIdx - 1;

            long val = pageIdLocksStack[last];

            if (val == pageId) {
                pageIdLocksStack[last] = 0;

                //Reset head to the first not empty element.
                do {
                    headIdx--;
                }
                while (headIdx - 1 >= 0 && pageIdLocksStack[headIdx - 1] == 0);
            }
            else {
                for (int i = last - 1; i >= 0; i--) {
                    if (pageIdLocksStack[i] == pageId) {
                        pageIdLocksStack[i] = 0;

                        return;
                    }
                }

                assert false : "Never should happened since with obtaine lock (push to stack) before unlock.\n"
                    + toString();
            }
        }
        else {
            long val = pageIdLocksStack[0];

            if (val == 0)
                throw new NoSuchElementException(
                    "Stack is empty, can not pop elemnt with cacheId=" +
                        cacheId + ", pageId=" + pageId + ", flags=" + hexInt(flags));

            if (val == pageId) {
                for (int i = 0; i < headIdx; i++)
                    pageIdLocksStack[i] = 0;

                headIdx = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }


    /** {@inheritDoc} */
    @Override public LocksStateSnapshot dump() {
        prepareDump();

        awaitLocks();

        long[] stack = copyOf(this.pageIdLocksStack, this.pageIdLocksStack.length);

        LocksStateSnapshot locksStateSnapshot = new LocksStateSnapshot(
            this.name + " (time=" + System.currentTimeMillis() + ")",
            headIdx,
            stack,
            nextOpPageId,
            nextOp
        );

        onDumpComplete();

        return locksStateSnapshot;
    }

    public static class LocksStateSnapshot implements Dump {
        public final String name;

        public final int headIdx;

        public final long[] pageIdLocksStack;

        public final long nextOpPageId;
        public final int nextOp;

        public LocksStateSnapshot(
            String name,
            int headIdx,
            long[] pageIdLocksStack,
            long panextOpPageIde,
            int nextOp
        ) {
            this.name = name;
            this.headIdx = headIdx;
            this.pageIdLocksStack = pageIdLocksStack;
            nextOpPageId = panextOpPageIde;
            this.nextOp = nextOp;
        }

        @Override public String toString() {
            SB res = new SB();

            res.a(name).a(", locked pages stack:\n");

            if (nextOpPageId != 0) {
                String str = "N/A";

                if (nextOp == BEFORE_READ_LOCK)
                    str = "obtain read lock";
                else if (nextOp == BEFORE_WRITE_LOCK)
                    str = "obtain write lock";

                res.a("\t-> try " + str + ", " + pageIdToString(nextOpPageId) + "\n");
            }

            for (int i = headIdx - 1; i >= 0; i--) {
                long pageId = pageIdLocksStack[i];

                if (pageId == 0 && i == 0)
                    break;

                if (pageId == 0) {
                    res.a("\t" + i + " -\n");
                }
                else {
                    res.a("\t" + i + " " + pageIdToString(pageId) + "\n");
                }
            }

            res.a("\n");

            return res.toString();
        }

        private String pageIdToString(long pageId) {
            return "pageId=" + pageId
                + " [pageIdxHex=" + hexLong(pageId)
                + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
                + ", flags=" + hexInt(flag(pageId)) + "]";
        }
    }
}

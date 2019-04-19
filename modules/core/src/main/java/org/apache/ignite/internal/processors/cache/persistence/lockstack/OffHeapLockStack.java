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

public class OffHeapLockStack implements LockStack {
    private long headPtr;
    private long headBasePtr;
    private static final int NUMBER_OF_ELEMENTS = 64;

    private static final int ELEMENT_SIZE = 16;

    private static final int CAPACITY = ELEMENT_SIZE * NUMBER_OF_ELEMENTS;

    @Override public void push(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        initIfNeed();

        if ((headPtr - headBasePtr) + ELEMENT_SIZE > CAPACITY)
            throw new StackOverflowError("Stack overflow, size:" + NUMBER_OF_ELEMENTS);

        long pageId0 = getLong(headPtr);

        assert pageId0 == 0L : "Head should be empty, headPtr=" + headPtr + ", pageId0=" + pageId0 + ", pageId=" + pageId;

        long pageMeta = (((long)flags) << 32) | ((long)cacheId);

        GridUnsafe.putLong(headPtr, pageId);
        GridUnsafe.putLong(headPtr + 8, pageMeta);

        headPtr += ELEMENT_SIZE;
    }

    @Override public void pop(int cacheId, long pageId, int flags) {
        assert pageId > 0;

        initIfNeed();

        if ((headPtr - headBasePtr) > ELEMENT_SIZE) {
            long last = headPtr - ELEMENT_SIZE;

            long pageId0 = getLong(last);

            if (pageId0 == pageId) {
                GridUnsafe.putLong(last, 0);
                GridUnsafe.putLong(last + 8, 0);

                headPtr--;
            }
            else {
                for (long off = last; off >= headBasePtr; off -= ELEMENT_SIZE) {
                    if (getLong(off) == pageId) {
                        GridUnsafe.putLong(off, 0);
                        GridUnsafe.putLong(last + 8, 0);

                        return;
                    }
                }

                assert false : "Never should happened since with obtaine lock (push to stack) before unlock.\n"
                    + toString();
            }
        }
        else {
            long pageId0 = getLong(headBasePtr);

            if (pageId0 == 0)
                throw new NoSuchElementException(
                    "Stack is empty, can not pop elemnt with cacheId=" +
                        cacheId + ", pageId=" + pageId + ", flags=" + hexInt(flags));

            if (pageId0 == pageId) {
                GridUnsafe.putLong(headBasePtr, 0);
                GridUnsafe.putLong(headBasePtr + 8, 0);

                headPtr = 0;
            }
            else
                // Corner case, we have only one elemnt on stack, but it not equals pageId for pop.
                assert false;
        }
    }

    @Override public int poistionIdx() {
        return (int)((headPtr - headBasePtr) / ELEMENT_SIZE);
    }

    @Override public int capacity() {
        return NUMBER_OF_ELEMENTS;
    }

    private void initIfNeed(){
        // Lazy init.
        if (headBasePtr == 0)
            headBasePtr = headPtr = allocate(CAPACITY);
    }

    public int head() {
        assert headPtr >= headBasePtr;

        return (int)((headPtr - headBasePtr) / ELEMENT_SIZE);
    }

    private long allocate(int size) {
        return GridUnsafe.allocateMemory(size);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        if (headBasePtr == 0)
            return "N/A";

        SB sb = new SB();

        sb.a("\n");

        for (long off = headBasePtr; off < headPtr; off += ELEMENT_SIZE) {
            long pageId = getLong(off);

            if (pageId == 0) {
                sb.a("-> [empty]\n" + (off + ELEMENT_SIZE < headPtr && getLong(off + ELEMENT_SIZE) == 0 ? "" : "\t"));

                continue;
            }

            long meta = GridUnsafe.getLong(off + 8);

            int op = (int)(meta >> 32);
            int cacheId = (int)(meta);

            String opStr = op == LockStack.READ ? "Read lock" : (op == LockStack.WRITE ? "Write lock" : "N/A");

            SB tab = new SB();

            long stackLevel = (off - headBasePtr) / ELEMENT_SIZE;

            for (int j = -1; j < stackLevel; j++)
                tab.a("\t");

            sb.a("L=" + stackLevel + " -> " + opStr + " pageId=" + pageId + ", cacheId=" + cacheId
                + " [pageIdxHex=" + hexLong(pageId)
                + ", partId=" + pageId(pageId) + ", pageIdx=" + pageIndex(pageId)
                + ", flags=" + hexInt(flag(pageId)) + "]\n" + tab);
        }

        return sb.toString();
    }
}

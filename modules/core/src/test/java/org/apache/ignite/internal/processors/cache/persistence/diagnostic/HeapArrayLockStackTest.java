package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.junit.Assert;
import org.junit.Test;

import static java.util.Arrays.stream;
import static org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker.BEFORE_READ_LOCK;

public class HeapArrayLockStackTest {
    private static final int CACHE_ID = 123;

    @Test
    public void testOneReadPageLock() {
        HeapArrayLockStack lockStack = new HeapArrayLockStack(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        LocksStackSnapshot dump;

        lockStack.onBeforeReadLock(CACHE_ID, pageId, page);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(pageId, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(CACHE_ID, pageId, page, pageAddr);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(CACHE_ID, pageId, page, pageAddr);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);
    }

    @Test
    public void testTwoReadPageLock() {
        HeapArrayLockStack lockStack = new HeapArrayLockStack(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long page1 = 2;
        long page2 = 12;
        long pageAddr1 = 3;
        long pageAddr2 = 13;

        lockStack.onBeforeReadLock(CACHE_ID, pageId1, page1);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        lockStack.onBeforeReadLock(CACHE_ID, pageId2, page2);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);
    }

    @Test
    public void testThreeReadPageLock_1() {
        HeapArrayLockStack lockStack = new HeapArrayLockStack(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        lockStack.onBeforeReadLock(CACHE_ID, pageId1, page1);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        lockStack.onBeforeReadLock(CACHE_ID, pageId2, page2);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId2, page2, pageAddr2);

        lockStack.onBeforeReadLock(CACHE_ID, pageId3, page3);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);
    }

    @Test
    public void testThreeReadPageLock_2() {
        HeapArrayLockStack lockStack = new HeapArrayLockStack(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        lockStack.onBeforeReadLock(CACHE_ID, pageId1, page1);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        lockStack.onBeforeReadLock(CACHE_ID, pageId2, page2);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        lockStack.onBeforeReadLock(CACHE_ID, pageId3, page3);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);
    }

    @Test
    public void testThreeReadPageLock_3() {
        HeapArrayLockStack lockStack = new HeapArrayLockStack(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        lockStack.onBeforeReadLock(CACHE_ID, pageId1, page1);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        lockStack.onBeforeReadLock(CACHE_ID, pageId2, page2);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId2, page2, pageAddr2);

        lockStack.onBeforeReadLock(CACHE_ID, pageId3, page3);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);
    }

    @Test
    public void testMultiThreadDump(){

    }

    private boolean isEmptyArray(long[] arr) {
        return stream(arr).filter(value -> value != 0).count() == 0;
    }
}
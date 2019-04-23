package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.junit.Test;

public class HeapArrayLockStackTest {
    private static final int CACHE_ID = 123;

    @Test
    public void testOneReadPageLock() {
        HeapArrayLockStack lockStack = new HeapArrayLockStack(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        lockStack.onBeforeReadLock(CACHE_ID, pageId, page);

        System.out.println(lockStack);

        lockStack.onReadLock(CACHE_ID, pageId, page, pageAddr);

        System.out.println(lockStack);

        lockStack.onReadUnlock(CACHE_ID, pageId, page, pageAddr);

        System.out.println(lockStack);
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
}
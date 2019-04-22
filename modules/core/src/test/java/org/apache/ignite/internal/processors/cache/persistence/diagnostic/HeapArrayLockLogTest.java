package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class HeapArrayLockLogTest {

    @Test
    public void testSimple() {
        LockInterceptor lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;

        lock.readLock(cacheId, pageId1);

        System.out.println(lock);

        lock.readLock(cacheId, pageId2);

        System.out.println(lock);

        lock.readLock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId2);

        System.out.println(lock);

        lock.readLock(cacheId, pageId1);

        System.out.println(lock);

        lock.readLock(cacheId, pageId2);

        System.out.println(lock);

        lock.readLock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId2);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId1);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId1);

        System.out.println(lock);

        Assert.assertEquals(0, lock.poistionIdx());
    }

    @Test
    public void testUnlockInTheMiddle() {
        LockInterceptor lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;

        lock.readLock(cacheId, pageId1);

        System.out.println(lock);

        lock.readLock(cacheId, pageId2);

        System.out.println(lock);

        lock.readLock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId2);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId1);

        System.out.println(lock);
    }

    @Test
    public void  testUnlockTwoInTheMiddle() {
        LockInterceptor lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;
        int pageId4 = 4;

        lock.beforeReadLock(cacheId, pageId1);

        System.out.println(lock);

        lock.readLock(cacheId, pageId1);

        System.out.println(lock);

        lock.beforeReadLock(cacheId, pageId2);

        System.out.println(lock);

        lock.readLock(cacheId, pageId2);

        System.out.println(lock);

        lock.beforeReadLock(cacheId, pageId3);

        System.out.println(lock);

        lock.readLock(cacheId, pageId3);

        System.out.println(lock);

        lock.readLock(cacheId, pageId4);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId2);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId4);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId1);

        System.out.println(lock);
    }

    @Test
    public void testUnlockInTheDowm() {
        LockInterceptor lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;

        lock.readLock(cacheId, pageId1);

        System.out.println(lock);

        lock.readLock(cacheId, pageId2);

        System.out.println(lock);

        lock.readLock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId1);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId2);

        System.out.println(lock);
    }

    @Test
    public void testUnlockTwoInTheDown() {
        LockInterceptor lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;
        int pageId4 = 4;

        lock.readLock(cacheId, pageId1);

        System.out.println(lock);

        lock.readLock(cacheId, pageId2);

        System.out.println(lock);

        lock.readLock(cacheId, pageId3);

        System.out.println(lock);

        lock.readLock(cacheId, pageId4);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId2);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId1);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId4);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId3);

        System.out.println(lock);
    }

    @Test
    public void testStackOverflow() {
        LockInterceptor lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        boolean excpetion = false;

        try {
            int pageId = pageId1;

            while (pageId < 10_000) {
                lock.readLock(cacheId, pageId);

                pageId++;
            }
        }
        catch (StackOverflowError e) {
            e.printStackTrace();

            excpetion = true;
        }

        if (!excpetion)
            fail();
    }

    private LockInterceptor create(String name) {
        return new HeapArrayLockLog(name, Thread.currentThread().getId());
    }
}
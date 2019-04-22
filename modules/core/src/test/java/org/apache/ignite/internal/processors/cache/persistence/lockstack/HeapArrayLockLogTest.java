package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class HeapArrayLockLogTest {

    @Test
    public void testSimple() {
        LockLog lock = create("test-name");

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
        LockLog lock = create("test-name");

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
        LockLog lock = create("test-name");

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

        lock.readUnlock(cacheId, pageId3);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId4);

        System.out.println(lock);

        lock.readUnlock(cacheId, pageId1);

        System.out.println(lock);
    }

    @Test
    public void testUnlockInTheDowm() {
        LockLog lock = create("test-name");

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
        LockLog lock = create("test-name");

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
        LockLog lock = create("test-name");

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
            excpetion = true;
        }

        if (!excpetion)
            fail();
    }

    private LockLog create(String name) {
        return new HeapArrayLockLog(name, Thread.currentThread().getId());
    }
}
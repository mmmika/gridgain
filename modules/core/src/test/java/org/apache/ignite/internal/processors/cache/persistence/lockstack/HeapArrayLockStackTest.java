package org.apache.ignite.internal.processors.cache.persistence.lockstack;

import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.ignite.internal.processors.cache.persistence.lockstack.LockStack.READ;
import static org.junit.Assert.*;

public class HeapArrayLockStackTest {

    @Test
    public void testSimple() {
        LockStack lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;

        lock.push(cacheId, pageId1, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId1, READ);

        System.out.println(lock);
    }

    @Test
    public void testUnlockInTheMiddle() {
        LockStack lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;

        lock.push(cacheId, pageId1, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId1, READ);

        System.out.println(lock);
    }

    @Test
    public void  testUnlockTwoInTheMiddle() {
        LockStack lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;
        int pageId4 = 4;

        lock.push(cacheId, pageId1, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId4, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId4, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId1, READ);

        System.out.println(lock);
    }

    @Test
    public void testUnlockInTheDowm() {
        LockStack lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;

        lock.push(cacheId, pageId1, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId2, READ);
        System.out.println(lock);

        lock.push(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId1, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId2, READ);

        System.out.println(lock);
    }

    @Test
    public void testUnlockTwoInTheDown() {
        LockStack lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        int pageId2 = 2;
        int pageId3 = 3;
        int pageId4 = 4;

        lock.push(cacheId, pageId1, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId3, READ);

        System.out.println(lock);

        lock.push(cacheId, pageId4, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId2, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId1, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId4, READ);

        System.out.println(lock);

        lock.pop(cacheId, pageId3, READ);

        System.out.println(lock);
    }

    @Test
    public void testExcpetionOnEmpty() {
        LockStack lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;

        boolean excpetion = false;

        try {
            lock.pop(cacheId, pageId1, READ);
        }
        catch (NoSuchElementException e) {
            excpetion = true;
        }

        if (!excpetion)
            fail();
    }

    @Test
    public void testStackOverflow() {
        LockStack lock = create("test-name");

        int cacheId = 123;
        int pageId1 = 1;
        boolean excpetion = false;

        try {
            int pageId = pageId1;

            while (pageId < 1000) {
                lock.push(cacheId, pageId, READ);

                pageId++;
            }
        }
        catch (StackOverflowError e) {
            excpetion = true;
        }

        if (!excpetion)
            fail();
    }

    private LockStack create(String name) {
        return new HeapArrayLockStack(name, Thread.currentThread().getId());
    }
}
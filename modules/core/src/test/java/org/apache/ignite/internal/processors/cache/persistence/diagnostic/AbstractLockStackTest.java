package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.IgniteInterruptedCheckedException;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.GridTestUtils;
import org.junit.Assert;
import org.junit.Test;

import static java.time.Duration.ofMinutes;
import static java.util.Arrays.stream;
import static org.apache.ignite.internal.processors.cache.persistence.diagnostic.AbstractPageLockTracker.BEFORE_READ_LOCK;

public abstract class AbstractLockStackTest {
    protected static final int STRUCTURE_ID = 123;

    protected abstract AbstractPageLockTracker<LocksStackSnapshot> createLockStackTracer(String name);

    @Test
    public void testOneReadPageLock() {
        AbstractPageLockTracker<LocksStackSnapshot> lockStack = createLockStackTracer(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        LocksStackSnapshot dump;

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId, page);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(Arrays.toString(dump.pageIdLocksStack), isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(pageId, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId, page, pageAddr);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId, page, pageAddr);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);
    }

    @Test
    public void testTwoReadPageLock() {
        AbstractPageLockTracker<LocksStackSnapshot> lockStack = createLockStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long page1 = 2;
        long page2 = 12;
        long pageAddr1 = 3;
        long pageAddr2 = 13;

        LocksStackSnapshot dump;

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(pageId1, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);
    }

    @Test
    public void testThreeReadPageLock_1() {
        AbstractPageLockTracker<LocksStackSnapshot> lockStack = createLockStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        LocksStackSnapshot dump;

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(pageId1, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId3, page3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(pageId3, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(3, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(pageId3, dump.pageIdLocksStack[2]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.pageIdLocksStack[2]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.pageIdLocksStack[2]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);
    }

    @Test
    public void testThreeReadPageLock_2() {
        AbstractPageLockTracker<LocksStackSnapshot> lockStack = createLockStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        LocksStackSnapshot dump;

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(pageId1, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId3, page3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId3, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId3, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);
    }

    @Test
    public void testThreeReadPageLock_3() {
        AbstractPageLockTracker<LocksStackSnapshot> lockStack = createLockStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        LocksStackSnapshot dump;

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(pageId1, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onBeforeReadLock(STRUCTURE_ID, pageId3, page3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(2, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(pageId3, dump.nextOpPageId);
        Assert.assertEquals(BEFORE_READ_LOCK, dump.nextOp);

        lockStack.onReadLock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(3, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(pageId2, dump.pageIdLocksStack[1]);
        Assert.assertEquals(pageId3, dump.pageIdLocksStack[2]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(3, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.pageIdLocksStack[1]);
        Assert.assertEquals(pageId3, dump.pageIdLocksStack[2]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(1, dump.headIdx);
        Assert.assertEquals(pageId1, dump.pageIdLocksStack[0]);
        Assert.assertEquals(0, dump.pageIdLocksStack[1]);
        Assert.assertEquals(0, dump.pageIdLocksStack[2]);
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);

        lockStack.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockStack);

        dump = lockStack.dump();

        Assert.assertEquals(0, dump.headIdx);
        Assert.assertTrue(isEmptyArray(dump.pageIdLocksStack));
        Assert.assertEquals(0, dump.nextOpPageId);
        Assert.assertEquals(0, dump.nextOp);
    }

    @Test
    public void testMultiThreadDump() throws IgniteCheckedException {
        AbstractPageLockTracker<LocksStackSnapshot> lockStack = createLockStackTracer(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        int cntDumps = 10_000;

        AtomicBoolean done = new AtomicBoolean();

        int maxWaitTime = 1000;

        int maxdeep = 16;

        IgniteInternalFuture f = GridTestUtils.runAsync(() -> {
            while (!done.get()) {
                int deep = nextRandomWaitTimeout(maxdeep);

                randomLocks(deep, () -> {
                    awaitRandom(100);

                    lockStack.onBeforeReadLock(STRUCTURE_ID, pageId, page);

                    awaitRandom(100);

                    lockStack.onReadLock(STRUCTURE_ID, pageId, page, pageAddr);
                });

                try {
                    awaitRandom(maxWaitTime);
                }
                finally {
                    randomLocks(deep, () -> {
                        lockStack.onReadUnlock(STRUCTURE_ID, pageId, page, pageAddr);
                    });
                }
            }
        });

        long totalExecutionTime = 0L;

        for (int i = 0; i < cntDumps; i++) {
            awaitRandom(50);

            long time = System.nanoTime();

            LocksStackSnapshot dump = lockStack.dump();

            long dumpTime = System.nanoTime() - time;

            if (dump.nextOp != 0)
                Assert.assertTrue(dump.nextOpPageId != 0);

            Assert.assertTrue(dump.time != 0);
            Assert.assertNotNull(dump.name);

            if (dump.headIdx > 0) {
                for (int j = 0; j < dump.headIdx; j++)
                    Assert.assertTrue(String.valueOf(dump.headIdx), dump.pageIdLocksStack[j] != 0);
            }

            Assert.assertNotNull(dump);

            totalExecutionTime += dumpTime;

            Assert.assertTrue(dumpTime <= ofMinutes((long)(maxWaitTime + (maxWaitTime * 0.1))).toNanos());

            if (i != 0 && i % 100 == 0)
                System.out.println(">>> Dump:" + i);
        }

        done.set(true);

        f.get();

        System.out.println(">>> Avarage time dump creation:" + (totalExecutionTime / cntDumps) + "ns");
    }

    private void randomLocks(int deep, Runnable r) {
        for (int i = 0; i < deep; i++)
            r.run();
    }

    private void awaitRandom(int bound) {
        try {
            U.sleep(nextRandomWaitTimeout(bound));
        }
        catch (IgniteInterruptedCheckedException e) {
            throw new IgniteException(e);
        }
    }

    private int nextRandomWaitTimeout(int bound) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        return rnd.nextInt(bound);
    }

    private boolean isEmptyArray(long[] arr) {
        return stream(arr).filter(value -> value != 0).count() == 0;
    }
}
package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.log.LockLog;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.log.LockLogSnapshot;
import org.apache.ignite.testframework.GridTestUtils;
import org.junit.Assert;
import org.junit.Test;

import static java.time.Duration.ofMinutes;
import static java.util.stream.IntStream.range;

public abstract class PageLockLogTest extends AbstractPageLockTest {
    protected static final int STRUCTURE_ID = 123;

    protected abstract LockLog createLogStackTracer(String name);

    @Test
    public void testOneReadPageLock() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        LockLogSnapshot log;

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId, page);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId, page, pageAddr);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId, page, pageAddr);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testTwoReadPageLock() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long page1 = 2;
        long page2 = 12;
        long pageAddr1 = 3;
        long pageAddr2 = 13;

        LockLogSnapshot log;

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testThreeReadPageLock_1() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        LockLogSnapshot log;

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId3, page3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testThreeReadPageLock_2() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        LockLogSnapshot log;

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId3, page3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testThreeReadPageLock_3() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;

        LockLogSnapshot log;

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId1, page1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId2, page2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId3, page3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testUnlockUnexcpected() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        LockLogSnapshot log;

        // Lock stack should be invalid after this operation because we can not unlock page
        // which was not locked.
        lockLog.onReadUnlock(STRUCTURE_ID, pageId, page, pageAddr);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testUnlockUnexcpectedOnNotEmptyStack() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long page1 = 2;
        long page2 = 12;
        long pageAddr1 = 3;
        long pageAddr2 = 13;

        LockLogSnapshot log;

        lockLog.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);

        // Lock stack should be invalid after this operation because we can not unlock page
        // which was not locked.
        lockLog.onReadUnlock(STRUCTURE_ID, pageId2, page2, pageAddr2);

        System.out.println(lockLog);

        log = lockLog.dump();

        Assert.assertTrue(lockLog.isInvalid());
        String msg = lockLog.invalidContext().msg;

        //TODO
    }

    @Test
    public void testUnlockUnexcpectedOnNotEmptyStackMultiLocks() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId1 = 1;
        long pageId2 = 11;
        long pageId3 = 111;
        long pageId4 = 1111;
        long page1 = 2;
        long page2 = 12;
        long page3 = 122;
        long page4 = 1222;
        long pageAddr1 = 3;
        long pageAddr2 = 13;
        long pageAddr3 = 133;
        long pageAddr4 = 1333;

        LockLogSnapshot log;

        lockLog.onReadLock(STRUCTURE_ID, pageId1, page1, pageAddr1);
        lockLog.onReadLock(STRUCTURE_ID, pageId2, page2, pageAddr2);
        lockLog.onReadLock(STRUCTURE_ID, pageId3, page3, pageAddr3);

        // Lock stack should be invalid after this operation because we can not unlock page
        // which was not locked.
        lockLog.onReadUnlock(STRUCTURE_ID, pageId4, page4, pageAddr4);

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testStackOverFlow() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        LockLogSnapshot log;

        // Lock stack should be invalid after this operation because we can get lock more that
        // stack capacity, +1 for overflow.
        range(0, lockLog.capacity() + 1).forEach((i) -> {
            lockLog.onReadLock(STRUCTURE_ID, pageId, page, pageAddr);
        });

        System.out.println(lockLog);

        log = lockLog.dump();

        //TODO
    }

    @Test
    public void testStackOperationAfterInvalid() {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        LockLogSnapshot log;

        // Lock stack should be invalid after this operation because we can not unlock page
        // which was not locked.
        lockLog.onReadUnlock(STRUCTURE_ID, pageId, page, pageAddr);

        log = lockLog.dump();

        //TODO

        System.out.println(lockLog.invalidContext());

        //TODO

        lockLog.onBeforeReadLock(STRUCTURE_ID, pageId, page);

        //TODO

        lockLog.onReadLock(STRUCTURE_ID, pageId, page, pageAddr);

        //TODO

        lockLog.onReadUnlock(STRUCTURE_ID, pageId, page, pageAddr);

        //TODO
    }

    @Test
    public void testThreadlog() throws IgniteCheckedException {
        LockLog lockLog = createLogStackTracer(Thread.currentThread().getName());

        long pageId = 1;
        long page = 2;
        long pageAddr = 3;

        int cntlogs = 10_000;

        AtomicBoolean done = new AtomicBoolean();

        int maxWaitTime = 1000;

        int maxdeep = 16;

        IgniteInternalFuture f = GridTestUtils.runAsync(() -> {
            while (!done.get()) {
                int deep = nextRandomWaitTimeout(maxdeep);

                randomLocks(deep, () -> {
                    awaitRandom(100);

                    lockLog.onBeforeReadLock(STRUCTURE_ID, pageId, page);

                    awaitRandom(100);

                    lockLog.onReadLock(STRUCTURE_ID, pageId, page, pageAddr);
                });

                try {
                    awaitRandom(maxWaitTime);
                }
                finally {
                    randomLocks(deep, () -> {
                        lockLog.onReadUnlock(STRUCTURE_ID, pageId, page, pageAddr);
                    });
                }
            }
        });

        long totalExecutionTime = 0L;

        for (int i = 0; i < cntlogs; i++) {
            awaitRandom(50);

            long time = System.nanoTime();

            LockLogSnapshot log = lockLog.dump();

            long logTime = System.nanoTime() - time;

            if (log.nextOp != 0)
                Assert.assertTrue(log.nextOpPageId != 0);

            Assert.assertTrue(log.time != 0);
            Assert.assertNotNull(log.name);

            if (log.headIdx > 0) {
                //TODO
               /* for (int j = 0; j < log.headIdx; j++)
                    Assert.assertTrue(String.valueOf(log.headIdx), log.pageIdLocksStack[j] != 0);*/
            }

            Assert.assertNotNull(log);

            totalExecutionTime += logTime;

            Assert.assertTrue(logTime <= ofMinutes((long)(maxWaitTime + (maxWaitTime * 0.1))).toNanos());

            if (i != 0 && i % 100 == 0)
                System.out.println(">>> log:" + i);
        }

        done.set(true);

        f.get();

        System.out.println(">>> Avarage time log creation:" + (totalExecutionTime / cntlogs) + " ns");
    }
}

/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.lang.utils;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import org.apache.ignite.IgniteInterruptedException;
import org.apache.ignite.internal.util.GridCircularBuffer;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 *
 */
public class GridCircularBufferSelfTest extends GridCommonAbstractTest {
    /**
     *
     */
    @Test
    public void testCreation() {
        try {
            GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(-2);

            assert false;

            info("Created buffer: " + buf);
        }
        catch (Exception e) {
            info("Caught expected exception: " + e);
        }

        try {
            GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(0);

            assert false;

            info("Created buffer: " + buf);
        }
        catch (Exception e) {
            info("Caught expected exception: " + e);
        }

        try {
            GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(5);

            assert false;

            info("Created buffer: " + buf);
        }
        catch (Exception e) {
            info("Caught expected exception: " + e);
        }

        GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(8);

        info("Created buffer: " + buf);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testSingleThreaded() throws Exception {
        int size = 8;
        int iterCnt = size * 10;

        GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(size);

        info("Created buffer: " + buf);

        Integer lastEvicted = null;

        for (int i = 0; i < iterCnt; i++) {
            Integer evicted = buf.add(i);

            info("Evicted: " + evicted);

            if (i >= size) {
                assert evicted != null;

                if (lastEvicted == null) {
                    lastEvicted = evicted;

                    continue;
                }

                assert lastEvicted + 1 == evicted : "Fail [lastEvicted=" + lastEvicted + ", evicted=" + evicted + ']';

                lastEvicted = evicted;
            }
        }
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testMutliThreaded() throws Exception {
        int size = 32 * 1024;

        final GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(size);
        final AtomicInteger itemGen = new AtomicInteger();

        info("Created buffer: " + buf);

        final int iterCnt = 1_000_000;

        multithreaded(new Callable<Object>() {
            @Override public Object call() throws Exception {
                for (int i = 0; i < iterCnt; i++) {
                    int item = itemGen.getAndIncrement();

                    buf.add(item);
                }

                return null;
            }
        }, 32);

        info("Buffer: " + buf);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testMutliThreaded2() throws Exception {
        int size = 256 * 1024;

        final GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(size);
        final AtomicInteger itemGen = new AtomicInteger();

        info("Created buffer: " + buf);

        final int iterCnt = 10_000;
        final Deque<Integer> evictedQ = new ConcurrentLinkedDeque<>();
        final Deque<Integer> putQ = new ConcurrentLinkedDeque<>();

        multithreaded(
            new Callable<Object>() {
                @Override public Object call() throws Exception {
                    for (int i = 0; i < iterCnt; i++) {
                        int item = itemGen.getAndIncrement();

                        putQ.add(item);

                        Integer evicted = buf.add(item);

                        if (evicted != null)
                            evictedQ.add(evicted);
                    }

                    return null;
                }
            },
            8);

        evictedQ.addAll(buf.items());

        assert putQ.containsAll(evictedQ);
        assert evictedQ.size() == putQ.size();

        info("Buffer: " + buf);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testEmptyBufIterator() throws Exception {
        assertFalse(new GridCircularBuffer<>(8).iterator().hasNext());
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testHalfFullBufIterator() throws Exception {
        int size = 8;

        GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(size);

        IntStream.range(0, size / 2).forEach(makeConsumer(buf));

        checkExpectedRange(0, size / 2, buf);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testFullBufIterator() throws Exception {
        int size = 8;

        GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(size);

        IntStream.range(0, size).forEach(makeConsumer(buf));

        checkExpectedRange(0, size, buf);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testOverflownBufIterator() throws Exception {
        int size = 8;

        GridCircularBuffer<Integer> buf = new GridCircularBuffer<>(size);

        IntStream.range(0, 3 * size / 2).forEach(makeConsumer(buf));

        checkExpectedRange(size / 2, 3 * size / 2, buf);
    }

    /**
     *
     */
    private static IntConsumer makeConsumer(GridCircularBuffer<Integer> buf) {
        return t -> {
            try {
                buf.add(t);
            }
            catch (InterruptedException e) {
                throw new IgniteInterruptedException(e);
            }
        };
    }

    /**
     *
     */
    private void checkExpectedRange(int beginInclusive, int endExclusive, GridCircularBuffer<Integer> buf) {
        Iterator<Integer> iter = buf.iterator();

        IntStream.range(beginInclusive, endExclusive).forEach(i -> assertEquals(i, iter.next().intValue()));

        assertFalse(iter.hasNext());
    }
}

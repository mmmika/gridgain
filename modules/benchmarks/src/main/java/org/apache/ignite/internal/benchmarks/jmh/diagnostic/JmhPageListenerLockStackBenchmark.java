package org.apache.ignite.internal.benchmarks.jmh.diagnostic;

import org.apache.ignite.internal.benchmarks.jmh.JmhAbstractBenchmark;
import org.apache.ignite.internal.benchmarks.jmh.runner.JmhIdeBenchmarkRunner;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.HeapArrayLockLog;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.HeapArrayLockStack;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.OffHeapLockStack;
import org.apache.ignite.internal.processors.cache.persistence.tree.util.PageLockListener;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

public class JmhPageListenerLockStackBenchmark extends JmhAbstractBenchmark {

    public static void main(String[] args) throws Exception {
        JmhIdeBenchmarkRunner.create()
            .forks(1)
            .threads(8)
            .warmupIterations(10)
            .measurementIterations(10)
            .benchmarks(JmhPageListenerLockStackBenchmark.class.getSimpleName())
            .jvmArguments("-Xms4g", "-Xmx4g")
            .run();

    }

    @State(Scope.Thread)
    public static class ThreadLocalState {
        PageLockListener pl;

        @Param({"2", "4", "8", "16"})
        int stackSize;

        @Param({"HeapArrayLockStack", "HeapArrayLockLog"})
        String type;

        int cacheId = 123;

        @Setup
        public void doSetup() {
            pl = create(Thread.currentThread().getName(), type);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void lockUnlock(ThreadLocalState localState) {
        PageLockListener pl = localState.pl;

        for (int i = 0; i < localState.stackSize; i++) {
            int pageId = i + 1;

            pl.onBeforeReadLock(localState.cacheId, pageId, pageId);

            pl.onReadLock(localState.cacheId, pageId, pageId, pageId);
        }

        for (int i = localState.stackSize; i > 0; i--) {
            int pageId = i;

            pl.onReadUnlock(localState.cacheId, pageId, pageId, pageId);
        }
    }

    private static PageLockListener create(String name, String type) {
        switch (type) {
            case "HeapArrayLockStack":
                return new HeapArrayLockStack(name);
            case "HeapArrayLockLog":
                return new HeapArrayLockLog(name);
        }

        return null;
    }
}

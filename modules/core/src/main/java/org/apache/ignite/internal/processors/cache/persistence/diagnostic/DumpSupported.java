package org.apache.ignite.internal.processors.cache.persistence.diagnostic;

import org.apache.ignite.lang.IgniteFuture;

public interface DumpSupported<T extends Dump> {

    T dump();

    IgniteFuture<T> dumpSync();
}

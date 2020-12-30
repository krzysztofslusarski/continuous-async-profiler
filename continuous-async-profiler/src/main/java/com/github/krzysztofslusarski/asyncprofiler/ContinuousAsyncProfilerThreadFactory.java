package com.github.krzysztofslusarski.asyncprofiler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class ContinuousAsyncProfilerThreadFactory implements ThreadFactory {
    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "cont-prof-" + counter.incrementAndGet());
    }
}

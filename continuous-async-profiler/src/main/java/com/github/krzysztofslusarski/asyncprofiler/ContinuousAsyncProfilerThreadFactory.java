package com.github.krzysztofslusarski.asyncprofiler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class ContinuousAsyncProfilerThreadFactory implements ThreadFactory {
    private AtomicInteger counter;

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "cont-prof-" + counter.incrementAndGet());
    }
}

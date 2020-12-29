/*
 * Copyright 2020 Krzysztof Slusarski, Michal Rowicki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.krzysztofslusarski.asyncprofiler;

import lombok.extern.slf4j.Slf4j;
import one.profiler.AsyncProfiler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ContinuousAsyncProfiler implements DisposableBean {

    private final List<Thread> threads = new ArrayList<>();

    public ContinuousAsyncProfiler(ContinuousAsyncProfilerProperties properties) {
        log.info("Staring with configuration: {}", properties);

        if (!properties.isEnabled()) {
            return;
        }

        createOutputDirectories(properties);
        AsyncProfiler asyncProfiler = StringUtils.isEmpty(properties.getProfilerLibPath()) ?
                AsyncProfiler.getInstance() : AsyncProfiler.getInstance(properties.getProfilerLibPath());

        threads.add(new Thread(new ContinuousAsyncProfilerRunner(asyncProfiler, properties), "cont-prof-runner"));
        threads.add(new Thread(new ContinuousAsyncProfilerCleaner(properties), "cont-prof-cleaner"));
        threads.add(new Thread(new ContinuousAsyncProfilerArchiver(properties), "cont-prof-arch"));
        threads.add(new Thread(new ContinuousAsyncProfilerCompressor(properties), "cont-prof-gzip"));
        log.info("Starting continuous profiling threads");
        threads.forEach(Thread::start);
    }

    private void createOutputDirectories(ContinuousAsyncProfilerProperties properties) {
        try {
            log.debug("Checking if output dirs exist");
            Files.createDirectories(Paths.get(properties.getArchiveOutputDir()));
            Files.createDirectories(Paths.get(properties.getContinuousOutputDir()));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create output dirs", e);
        }
    }

    @Override
    public void destroy() {
        log.info("Spring context destroyed, shutting down threads");
        threads.forEach(Thread::interrupt);
    }
}

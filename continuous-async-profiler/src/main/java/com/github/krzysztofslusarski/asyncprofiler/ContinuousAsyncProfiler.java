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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import one.profiler.AsyncProfiler;
import one.profiler.AsyncProfilerLoader;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

@Slf4j
public class ContinuousAsyncProfiler implements DisposableBean {
    private final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();
    private final ThreadFactory threadFactory = new ContinuousAsyncProfilerThreadFactory();
    private final ScheduledExecutorService mainExecutorService = new ScheduledThreadPoolExecutor(1, threadFactory);
    private final ScheduledExecutorService helperExecutorService = new ScheduledThreadPoolExecutor(1, threadFactory);
    private final ContinuousAsyncProfilerRunner profilerRunner;

    public ContinuousAsyncProfiler(ContinuousAsyncProfilerManageablePropertiesRepository manageablePropertiesRepository,
                                   ContinuousAsyncProfilerNotManageableProperties notManageableProperties) {
        ContinuousAsyncProfilerManageableProperties manageableProperties = manageablePropertiesRepository.getManageableProperties();
        log.info("Staring with configuration: {} {}", manageableProperties, notManageableProperties);

        if (!notManageableProperties.isLoadNativeLibrary()) {
            profilerRunner = null;
            return;
        }

        createOutputDirectories(notManageableProperties);
        AsyncProfiler asyncProfiler = null;
        try {
            asyncProfiler = StringUtils.isEmpty(notManageableProperties.getProfilerLibPath()) ?
                    AsyncProfilerLoader.load() : AsyncProfiler.getInstance(notManageableProperties.getProfilerLibPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Starting continuous profiling threads");
        profilerRunner = new ContinuousAsyncProfilerRunner(asyncProfiler, manageablePropertiesRepository, notManageableProperties);
        scheduledFutures.add(mainExecutorService.scheduleAtFixedRate(
                profilerRunner, 0, notManageableProperties.getDumpIntervalSeconds(), TimeUnit.SECONDS
        ));
        scheduledFutures.add(helperExecutorService.scheduleAtFixedRate(
                new ContinuousAsyncProfilerCleaner(manageablePropertiesRepository, notManageableProperties), 0, 1, TimeUnit.HOURS
        ));
        scheduledFutures.add(helperExecutorService.scheduleAtFixedRate(
                new ContinuousAsyncProfilerArchiver(manageablePropertiesRepository, notManageableProperties), 0, 1, TimeUnit.DAYS
        ));
        int compressionIntervalSeconds = notManageableProperties.getCompressionIntervalSeconds();
        if (compressionIntervalSeconds > 0) {
            scheduledFutures.add(helperExecutorService.scheduleAtFixedRate(
                    new ContinuousAsyncProfilerCompressor(notManageableProperties), 0, compressionIntervalSeconds, TimeUnit.SECONDS
            ));
        }
    }

    private void createOutputDirectories(ContinuousAsyncProfilerNotManageableProperties properties) {
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
        mainExecutorService.shutdown();
        helperExecutorService.shutdown();
        scheduledFutures.forEach(scheduledFuture -> scheduledFuture.cancel(false));
        if (profilerRunner != null) {
            profilerRunner.shutdown();
        }
    }
}
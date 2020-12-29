/*
 * Copyright 2020 Krzysztof Slusarski
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
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import one.profiler.AsyncProfiler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class ContinuousAsyncProfilerConfiguration {
    private final List<Thread> threads = new ArrayList<>();

    public ContinuousAsyncProfilerConfiguration(
            @Value("${asyncProfiler.continuous.enabled:true}") boolean enabled,
            @Value("${asyncProfiler.continuous.dumpIntervalSeconds:60}") int dumpIntervalSeconds,
            @Value("${asyncProfiler.continuous.continuousOutputsMaxAgeHours:24}") int continuousOutputsMaxAgeHours,
            @Value("${asyncProfiler.continuous.archiveOutputsMaxAgeDays:30}") int archiveOutputsMaxAgeDays,
            @Value("${asyncProfiler.continuous.archiveCopyRegex:.*_13:0.*}") String archiveCopyRegex,
            @Value("${asyncProfiler.continuous.event:wall}") String event,
            @Value("${asyncProfiler.continuous.profilerLibPath:}") String profilerLibPath,
            @Value("${asyncProfiler.continuous.stopWorkFile:profiler-stop}") String stopFile,
            @Value("${asyncProfiler.continuous.outputDir.archive:logs/archive}") String outputDirArchive,
            @Value("${asyncProfiler.continuous.outputDir.continuous:logs/continuous}") String outputDirContinuous
    ) {
        ContinuousAsyncProfilerProperties properties = ContinuousAsyncProfilerProperties.builder()
                .enabled(enabled)
                .event(event)
                .stopFile(stopFile)
                .dumpIntervalSeconds(dumpIntervalSeconds)
                .continuousOutputsMaxAgeHours(continuousOutputsMaxAgeHours)
                .archiveOutputsMaxAgeDays(archiveOutputsMaxAgeDays)
                .archiveCopyRegex(archiveCopyRegex)
                .continuousOutputDir(outputDirContinuous)
                .archiveOutputDir(outputDirArchive)
                .profilerLibPath(profilerLibPath)
                .build();

        log.info("Staring with configuration: {}", properties);

        if (!properties.isEnabled()) {
            return;
        }

        createOutputDirectories(properties);
        AsyncProfiler asyncProfiler = StringUtils.isEmpty(profilerLibPath) ? AsyncProfiler.getInstance() : AsyncProfiler.getInstance(profilerLibPath);

        threads.add(new Thread(new ContinuousAsyncProfilerRunner(asyncProfiler, properties), "cont-prof-runner"));
        threads.add(new Thread(new ContinuousAsyncProfilerCleaner(properties), "cont-prof-cleaner"));
        threads.add(new Thread(new ContinuousAsyncProfilerArchiver(properties), "cont-prof-arch"));
        threads.add(new Thread(new ContinuousAsyncProfilerCompressor(properties), "cont-prof-gzip"));
        log.info("Starting continuous profiling threads");
        threads.forEach(Thread::start);
    }

    @PreDestroy
    void shutdown() {
        log.info("Spring context destroyed, shutting down threads");
        threads.forEach(Thread::interrupt);
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
}

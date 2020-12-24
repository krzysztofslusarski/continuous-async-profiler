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
package pl.ks.profiling.asyncprofiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import one.profiler.AsyncProfiler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ContinuousAsyncProfilerConfiguration {
    public ContinuousAsyncProfilerConfiguration(
            @Value("${asyncProfiler.continuous.enabled:true}") boolean enabled,
            @Value("${asyncProfiler.continuous.dumpIntervalSeconds:60}") int dumpIntervalSeconds,
            @Value("${asyncProfiler.continuous.continuousOutputsMaxAgeHours:24}") int continuousOutputsMaxAgeHours,
            @Value("${asyncProfiler.continuous.archiveOutputsMaxAgeDays:30}") int archiveOutputsMaxAgeDays,
            @Value("${asyncProfiler.continuous.event:.*_13:0.*}") String archiveCopyRegex,
            @Value("${asyncProfiler.continuous.event:wall}") String event,
            @Value("${asyncProfiler.continuous.outputDir.archive:logs/archive}") String outputDirArchive,
            @Value("${asyncProfiler.continuous.outputDir.continuous:logs/continuous}") String outputDirContinuous
    ) {
        ContinuousAsyncProfilerProperties properties = ContinuousAsyncProfilerProperties.builder()
                .enabled(enabled)
                .event(event)
                .dumpIntervalSeconds(dumpIntervalSeconds)
                .continuousOutputsMaxAgeHours(continuousOutputsMaxAgeHours)
                .archiveOutputsMaxAgeDays(archiveOutputsMaxAgeDays)
                .archiveCopyRegex(archiveCopyRegex)
                .continuousOutputDir(outputDirContinuous)
                .archiveOutputDir(outputDirArchive)
                .build();

        log.info("Staring with configuration: {}", properties);

        if (!properties.isEnabled()) {
            return;
        }

        createOutputDirectories(properties);

        AsyncProfiler asyncProfiler = AsyncProfiler.getInstance();

        new Thread(new ContinuousAsyncProfilerRunner(asyncProfiler, properties), "cont-prof-runner").start();
        new Thread(new ContinuousAsyncProfilerCleaner(properties), "cont-prof-cleaner").start();
        new Thread(new ContinuousAsyncProfilerArchiver(properties), "cont-prof-arch").start();
        new Thread(new ContinuousAsyncProfilerCompressor(properties), "cont-prof-gzip").start();
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

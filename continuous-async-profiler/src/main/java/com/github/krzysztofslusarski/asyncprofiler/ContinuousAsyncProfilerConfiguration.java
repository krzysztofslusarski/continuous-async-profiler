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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ContinuousAsyncProfilerConfiguration {

    private final ContinuousAsyncProfilerProperties properties;

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
        this.properties = ContinuousAsyncProfilerProperties.builder()
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
    }

    @Bean
    ContinuousAsyncProfiler continuousAsyncProfiler() {
        return new ContinuousAsyncProfiler(properties);
    }
}

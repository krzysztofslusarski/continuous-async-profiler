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

import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import(ContinuousAsyncProfilerMBeanConfiguration.class)
public class ContinuousAsyncProfilerConfiguration {

    private final ContinuousAsyncProfilerProperties properties;

    public ContinuousAsyncProfilerConfiguration(
            @Value("${async-profiler.continuous.enabled:true}") boolean enabled,
            @Value("${async-profiler.continuous.dump-interval:60}") int dumpIntervalSeconds,
            @Value("${async-profiler.continuous.continuous-outputs-max-age-hours:24}") int continuousOutputsMaxAgeHours,
            @Value("${async-profiler.continuous.archive-outputs-max-age-days:30}") int archiveOutputsMaxAgeDays,
            @Value("${async-profiler.continuous.archive-copy-regex:.*_13:0.*}") String archiveCopyRegex,
            @Value("${async-profiler.continuous.event:wall}") String event,
            @Value("${async-profiler.continuous.profiler-lib-path:}") String profilerLibPath,
            @Value("${async-profiler.continuous.stop-work-file:profiler-stop}") String stopFile,
            @Value("${async-profiler.continuous.output-dir.archive:logs/archive}") String outputDirArchive,
            @Value("${async-profiler.continuous.output-dir.continuous:logs/continuous}") String outputDirContinuous
    ) {
        this.properties = ContinuousAsyncProfilerProperties.builder()
                .enabled(enabled)
                .event(event)
                .stopFile(stopFile)
                .dumpIntervalSeconds(dumpIntervalSeconds)
                .continuousOutputsMaxAgeHours(continuousOutputsMaxAgeHours)
                .archiveOutputsMaxAgeDays(archiveOutputsMaxAgeDays)
                .compiledArchiveCopyRegex(Pattern.compile(archiveCopyRegex))
                .continuousOutputDir(outputDirContinuous)
                .archiveOutputDir(outputDirArchive)
                .profilerLibPath(profilerLibPath)
                .build();
    }

    @Bean
    ContinuousAsyncProfiler continuousAsyncProfiler(ContinuousAsyncProfilerMBeanPropertiesService continuousAsyncProfilerMBeanPropertiesService) {
        return new ContinuousAsyncProfiler(properties, continuousAsyncProfilerMBeanPropertiesService);
    }
}

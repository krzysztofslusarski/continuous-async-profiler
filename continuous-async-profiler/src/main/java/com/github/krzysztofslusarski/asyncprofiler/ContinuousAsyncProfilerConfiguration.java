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

import com.github.krzysztofslusarski.asyncprofiler.mbean.ContinuousAsyncProfilerMBeanConfiguration;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import(ContinuousAsyncProfilerMBeanConfiguration.class)
@Conditional(ContinuousAsyncProfilerCondition.class)
public class ContinuousAsyncProfilerConfiguration {
    @Bean
    ContinuousAsyncProfilerManageableProperties defaultManageableProperties(
            @Value("${async-profiler.continuous.enabled:true}") boolean enabled,
            @Value("${async-profiler.continuous.continuous-outputs-max-age-hours:24}") int continuousOutputsMaxAgeHours,
            @Value("${async-profiler.continuous.archive-outputs-max-age-days:30}") int archiveOutputsMaxAgeDays,
            @Value("${async-profiler.continuous.archive-copy-regex:.*_13:0.*}") String archiveCopyRegex,
            @Value("${async-profiler.continuous.event:wall}") String event,
            @Value("${async-profiler.continuous.stop-work-file:profiler-stop}") String stopFile,
            @Value("${async-profiler.continuous.additional-parameters:}") String additionalParameters,
            @Value("${async-profiler.continuous.prefix:}") String prefix
            ) {
        return ContinuousAsyncProfilerManageableProperties.builder()
                .enabled(enabled)
                .event(event)
                .stopFile(stopFile)
                .continuousOutputsMaxAgeHours(continuousOutputsMaxAgeHours)
                .archiveOutputsMaxAgeDays(archiveOutputsMaxAgeDays)
                .compiledArchiveCopyRegex(Pattern.compile(archiveCopyRegex))
                .additionalParameters(additionalParameters)
                .prefix(prefix)
                .build();
    }

    @Bean
    ContinuousAsyncProfilerNotManageableProperties defaultNotManageableProperties(
            @Value("${async-profiler.continuous.load-native-library:true}") boolean loadNativeLibrary,
            @Value("${async-profiler.continuous.dump-interval:60}") int dumpIntervalSeconds,
            @Value("${async-profiler.continuous.compression-interval:600}") int compressionInterval,
            @Value("${async-profiler.continuous.profiler-lib-path:}") String profilerLibPath,
            @Value("${async-profiler.continuous.output-dir.archive:logs/archive}") String outputDirArchive,
            @Value("${async-profiler.continuous.output-dir.continuous:logs/continuous}") String outputDirContinuous
    ) {
        return ContinuousAsyncProfilerNotManageableProperties.builder()
                .loadNativeLibrary(loadNativeLibrary)
                .dumpIntervalSeconds(dumpIntervalSeconds)
                .continuousOutputDir(outputDirContinuous)
                .compressionIntervalSeconds(compressionInterval)
                .archiveOutputDir(outputDirArchive)
                .profilerLibPath(profilerLibPath)
                .build();
    }

    @Bean
    ContinuousAsyncProfiler continuousAsyncProfiler(ContinuousAsyncProfilerManageableProperties defaultManageableProperties,
                                                    ContinuousAsyncProfilerNotManageableProperties defaultNotManageableProperties,
                                                    @Autowired(required = false) ContinuousAsyncProfilerManageablePropertiesRepository manageablePropertiesRepository) {
        if (manageablePropertiesRepository == null) {
            manageablePropertiesRepository = () -> defaultManageableProperties;
        }

        return new ContinuousAsyncProfiler(manageablePropertiesRepository, defaultNotManageableProperties);
    }
}

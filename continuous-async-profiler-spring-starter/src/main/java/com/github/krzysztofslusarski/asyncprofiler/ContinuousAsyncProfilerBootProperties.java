/*
 * Copyright 2020 Michal Rowicki
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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Pattern;

@Data
@ConfigurationProperties("async-profiler.continuous")
class ContinuousAsyncProfilerBootProperties {
    /**
     * if the tool should work or not
     */
    private boolean enabled = true;
    /**
     * custom path to libasyncProfiler.so
     */
    private Path profilerLibPath;
    /**
     * async-profiler event to fetch
     */
    private String event = "wall";
    /**
     * path to a file, if the file exists then profiler is not running, using this file you can turn
     * on/off profiling at runtime
     */
    private Path stopWorkFile = Paths.get("profiler-stop");

    private OutputDir outputDir;

    @Data
    static class OutputDir {
        /**
         * where continuous output should be stored
         */
        private Path continuous = Paths.get("logs/continuous");
        /**
         * where archive of the outputs should be stored
         */
        private Path archive = Paths.get("logs/archive");
    }

    /**
     * duration of how often tool should dump profiler outputs
     */
    private Duration dumpInterval = Duration.ofSeconds(60);
    /**
     * time in hours, how long to keep files in the continuous directory
     */
    private Duration continuousOutputsMaxAgeHours = Duration.ofHours(24);
    /**
     * time in days, how long to keep files in the archive directory
     */
    private Duration archiveOutputsMaxAgeDays = Duration.ofDays(30);
    /**
     * regex for file name, which files should be copied from the continuous to the archive directory
     */
    private Pattern archiveCopyRegex = Pattern.compile(".*_13:0.*");

    public ContinuousAsyncProfilerProperties toSpringFrameworkProperties() {
        return ContinuousAsyncProfilerProperties.builder()
                .enabled(enabled)
                .event(event == null || event.isEmpty() ? "wall" : event)
                .stopFile(stopWorkFile == null ? "profiler-stop" : stopWorkFile.toString())
                .dumpIntervalSeconds(dumpInterval == null ? 60 : (int)dumpInterval.getSeconds())
                .continuousOutputsMaxAgeHours(continuousOutputsMaxAgeHours == null ? 24 : (int) continuousOutputsMaxAgeHours.toHours())
                .archiveOutputsMaxAgeDays(archiveOutputsMaxAgeDays == null ? 30 : (int) archiveOutputsMaxAgeDays.toDays())
                .archiveCopyRegex(archiveCopyRegex == null ? ".*_13:0.*" : archiveCopyRegex.pattern())
                .continuousOutputDir(outputDir.continuous == null ? "logs/continuous" : outputDir.continuous.toString())
                .archiveOutputDir(outputDir.archive == null ? "logs/archive" : outputDir.archive.toString())
                .profilerLibPath(profilerLibPath == null ? "" : profilerLibPath.toString())
                .build();
    }
}

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

import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ContinuousAsyncProfilerProperties {
    boolean enabled;
    String profilerLibPath;
    String event;
    String stopFile;
    String continuousOutputDir;
    String archiveOutputDir;
    int dumpIntervalSeconds;
    int continuousOutputsMaxAgeHours;
    int archiveOutputsMaxAgeDays;
    Pattern compiledArchiveCopyRegex;

    long dumpIntervalMilliseconds() {
        return dumpIntervalSeconds * 1000L;
    }
}

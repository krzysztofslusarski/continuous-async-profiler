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
package com.github.krzysztofslusarski.asyncprofiler.mbean;

import com.github.krzysztofslusarski.asyncprofiler.ContinuousAsyncProfilerManageableProperties;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@RequiredArgsConstructor
@ManagedResource("com.github.krzysztofslusarski:type=AsyncProfiler.Continuous")
public class ContinuousAsyncProfilerMBean {
    private final ContinuousAsyncProfilerMBeanPropertiesService propertiesService;

    @ManagedOperation
    public void resetPropertiesToDefaults() {
        propertiesService.resetToDefaults();
    }

    @ManagedAttribute(description = "if the tool should work or not")
    public boolean isEnabled() {
        return propertiesService.getProperties().isEnabled();
    }

    @ManagedOperation
    public void disable() {
        ContinuousAsyncProfilerManageableProperties newProperties = propertiesService.getProperties().withEnabled(false);
        propertiesService.setProperties(newProperties);
    }

    @ManagedOperation
    public void enable() {
        ContinuousAsyncProfilerManageableProperties newProperties = propertiesService.getProperties().withEnabled(true);
        propertiesService.setProperties(newProperties);
    }

    @ManagedAttribute(description = "async-profiler event to fetch")
    public String getEvent() {
        return propertiesService.getProperties().getEvent();
    }

    @ManagedOperation
    public void setEvent(String event) {
        ContinuousAsyncProfilerManageableProperties newProperties = propertiesService.getProperties().withEvent(event);
        propertiesService.setProperties(newProperties);
    }

    @ManagedAttribute(description = "path to a file, if the file exists then profiler is not running, using this file you can turn on/off profiling at runtime")
    public String getStopFile() {
        return propertiesService.getProperties().getStopFile();
    }

    @ManagedOperation
    public void setStopFile(String stopFile) {
        ContinuousAsyncProfilerManageableProperties newProperties = propertiesService.getProperties().withStopFile(stopFile);
        propertiesService.setProperties(newProperties);
    }

    @ManagedAttribute(description = "time in days, how long to keep files in the archive directory")
    public int getArchiveOutputsMaxAgeDays() {
        return propertiesService.getProperties().getArchiveOutputsMaxAgeDays();
    }

    @ManagedOperation
    public void setArchiveOutputsMaxAgeDays(int archiveOutputsMaxAgeDays) {
        ContinuousAsyncProfilerManageableProperties newProperties = propertiesService.getProperties().withArchiveOutputsMaxAgeDays(archiveOutputsMaxAgeDays);
        propertiesService.setProperties(newProperties);
    }

    @ManagedAttribute(description = "time in hours, how long to keep files in the continuous directory")
    public int getContinuousOutputsMaxAgeHours() {
        return propertiesService.getProperties().getContinuousOutputsMaxAgeHours();
    }

    @ManagedOperation
    public void setContinuousOutputsMaxAgeHours(int continuousOutputsMaxAgeHours) {
        ContinuousAsyncProfilerManageableProperties newProperties = propertiesService.getProperties().withContinuousOutputsMaxAgeHours(continuousOutputsMaxAgeHours);
        propertiesService.setProperties(newProperties);
    }

    @ManagedAttribute(description = "regex for file name, which files should be copied from the continuous to the archive directory")
    public String getArchiveCopyRegex() {
        return propertiesService.getProperties().getCompiledArchiveCopyRegex().pattern();
    }

    @ManagedOperation
    public void setArchiveCopyRegex(String regex) {
        ContinuousAsyncProfilerManageableProperties newProperties = propertiesService.getProperties().withCompiledArchiveCopyRegex(Pattern.compile(regex));
        propertiesService.setProperties(newProperties);
    }
}

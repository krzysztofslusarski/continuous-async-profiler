package pl.ks.profiling.asyncprofiler;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ContinuousAsyncProfilerProperties {
    boolean enabled;
    String event;
    String outputDir;
    String continuousOutputDir;
    String archiveOutputDir;
    int dumpIntervalSeconds;
    int continuousOutputsMaxAgeHours;
    int archiveOutputsMaxAgeDays;
    String archiveCopyRegex;
}

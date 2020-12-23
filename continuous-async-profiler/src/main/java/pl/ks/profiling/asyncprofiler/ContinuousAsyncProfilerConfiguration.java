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
            @Value("${asyncProfiler.continuous.outputDir:logs}") String outputDir
    ) {
        ContinuousAsyncProfilerProperties properties = ContinuousAsyncProfilerProperties.builder()
                .enabled(enabled)
                .event(event)
                .outputDir(outputDir)
                .dumpIntervalSeconds(dumpIntervalSeconds)
                .continuousOutputsMaxAgeHours(continuousOutputsMaxAgeHours)
                .archiveOutputsMaxAgeDays(archiveOutputsMaxAgeDays)
                .archiveCopyRegex(archiveCopyRegex)
                .continuousOutputDir(outputDir + "/continuous")
                .archiveOutputDir(outputDir + "/archive")
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
            Files.createDirectories(Paths.get(properties.getOutputDir()));
            Files.createDirectories(Paths.get(properties.getArchiveOutputDir()));
            Files.createDirectories(Paths.get(properties.getContinuousOutputDir()));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create output dirs", e);
        }
    }
}

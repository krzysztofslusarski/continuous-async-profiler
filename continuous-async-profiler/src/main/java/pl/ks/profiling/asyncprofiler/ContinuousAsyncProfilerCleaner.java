package pl.ks.profiling.asyncprofiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ContinuousAsyncProfilerCleaner implements Runnable {
    private static final long ONE_MINUTE = 1000 * 60;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = ONE_HOUR * 24;
    private final ContinuousAsyncProfilerProperties properties;

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        while (!Thread.interrupted()) {
            try {
                long currentTime = System.currentTimeMillis();
                long continuousCutOffTime = currentTime - (properties.getContinuousOutputsMaxAgeHours() * ONE_HOUR);
                long archiveCutOffTime = currentTime - (properties.getArchiveOutputsMaxAgeDays() * ONE_DAY);
                log.info("Removing old continuous output");
                delete(properties.getContinuousOutputDir(), continuousCutOffTime);
                log.info("Removing old archive output");
                delete(properties.getArchiveOutputDir(), archiveCutOffTime);
                Thread.sleep(ONE_HOUR);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void delete(String cleanDir, long cutOffTime) {
        try {
            Files.list(Paths.get(cleanDir))
                    .filter(path -> {
                        try {
                            return Files.isRegularFile(path) && Files.getLastModifiedTime(path).toMillis() < cutOffTime;
                        } catch (IOException e) {
                            log.error("Cannot fetch file information: " + path.toAbsolutePath().toString(), e);
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("Cannot delete file: " + path.toAbsolutePath().toString(), e);
                        }
                    });
        } catch (IOException e) {
            log.error("Cannot fetch file list in dir: " + cleanDir, e);
        }
    }
}

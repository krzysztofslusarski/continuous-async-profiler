package pl.ks.profiling.asyncprofiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ContinuousAsyncProfilerArchiver implements Runnable {
    private static final long ONE_MINUTE = 1000 * 60;
    private static final long ONE_DAY = ONE_MINUTE * 60 * 24;

    private final ContinuousAsyncProfilerProperties properties;

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        Path continuousDir = Paths.get(properties.getContinuousOutputDir());
        BiPredicate<Path, BasicFileAttributes> predicate = (p, basicFileAttributes) -> p.getFileName().toString().matches(properties.getArchiveCopyRegex());
        while (!Thread.interrupted()) {
            try {
                Files.find(continuousDir, 1, predicate)
                        .forEach(sourcePath -> {
                            String fileName = sourcePath.getFileName().toString();
                            String destinationFileName = properties.getArchiveOutputDir() + "/" + fileName;
                            Path destinationPath = Paths.get(destinationFileName);
                            if (!destinationPath.toFile().exists()) {
                                log.info("Archiving: {} to: {}", fileName, destinationPath.toAbsolutePath().toString());
                                try {
                                    Files.copy(sourcePath, destinationPath);
                                } catch (IOException e) {
                                    log.error("Cannot copy file", e);
                                }
                            } else {
                                log.info("Will not archive, file exists in archive dir: {}", fileName);
                            }
                        });
                Thread.sleep(ONE_DAY);
            } catch (InterruptedException e) {
                log.error("Cannot list dir: " + properties.getContinuousOutputDir(), e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

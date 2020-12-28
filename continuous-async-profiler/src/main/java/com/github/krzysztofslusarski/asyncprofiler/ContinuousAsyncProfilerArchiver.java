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
                Thread.sleep(SleepTime.ONE_DAY);
            } catch (InterruptedException e) {
                log.info("Thread interrupted, exiting", e);
                Thread.currentThread().interrupt();
                return;
            } catch (IOException e) {
                log.error("Cannot list dir: " + properties.getContinuousOutputDir(), e);
            }
        }
    }

}

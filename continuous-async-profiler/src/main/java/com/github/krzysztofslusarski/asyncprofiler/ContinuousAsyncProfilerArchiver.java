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
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ContinuousAsyncProfilerArchiver implements Runnable {
    private final ContinuousAsyncProfilerManageablePropertiesRepository manageablePropertiesRepository;
    private final ContinuousAsyncProfilerNotManageablePropertiesRepository notManageablePropertiesRepository;

    private final Path continuousDir;
    private final BiPredicate<Path, BasicFileAttributes> predicate;

    public ContinuousAsyncProfilerArchiver(ContinuousAsyncProfilerManageablePropertiesRepository manageablePropertiesRepository,
                                           ContinuousAsyncProfilerNotManageablePropertiesRepository notManageablePropertiesRepository) {
        this.manageablePropertiesRepository = manageablePropertiesRepository;
        this.notManageablePropertiesRepository = notManageablePropertiesRepository;

        ContinuousAsyncProfilerNotManageableProperties notManageableProperties = notManageablePropertiesRepository.getAsyncProfilerNotManageableProperties();

        this.continuousDir = Paths.get(notManageableProperties.getContinuousOutputDir());
        this.predicate = (p, ignore) -> manageablePropertiesRepository.getManageableProperties().getCompiledArchiveCopyRegex().matcher(p.getFileName().toString()).matches() && Files.isRegularFile(p);
    }

    @Override
    public void run() {
        ContinuousAsyncProfilerNotManageableProperties notManageableProperties = notManageablePropertiesRepository.getAsyncProfilerNotManageableProperties();

        try (Stream<Path> archiveStream = Files.find(continuousDir, 1, predicate)) {
            archiveStream.forEach(sourcePath -> {
                String fileName = sourcePath.getFileName().toString();
                Path destinationPath = Paths.get(notManageableProperties.getArchiveOutputDir(), fileName);
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
        } catch (IOException e) {
            log.error("Cannot list dir: " + notManageableProperties.getContinuousOutputDir(), e);
        }
    }
}


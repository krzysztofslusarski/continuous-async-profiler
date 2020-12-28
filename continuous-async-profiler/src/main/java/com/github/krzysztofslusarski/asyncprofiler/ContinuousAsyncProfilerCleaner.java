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
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ContinuousAsyncProfilerCleaner implements Runnable {
    private final ContinuousAsyncProfilerProperties properties;

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        while (!Thread.interrupted()) {
            try {
                long currentTime = System.currentTimeMillis();
                long continuousCutOffTime = currentTime - (properties.getContinuousOutputsMaxAgeHours() * SleepTime.ONE_HOUR);
                long archiveCutOffTime = currentTime - (properties.getArchiveOutputsMaxAgeDays() * SleepTime.ONE_DAY);
                log.info("Removing old continuous output");
                delete(properties.getContinuousOutputDir(), continuousCutOffTime);
                log.info("Removing old archive output");
                delete(properties.getArchiveOutputDir(), archiveCutOffTime);
                Thread.sleep(SleepTime.ONE_HOUR);
            } catch (InterruptedException e) {
                log.info("Thread interrupted, exiting", e);
                return;
            }
        }
    }

    public void delete(String cleanDir, long cutOffTime) {
        try (Stream<Path> list = Files.list(Paths.get(cleanDir))) {
            list
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

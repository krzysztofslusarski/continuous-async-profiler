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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ContinuousAsyncProfilerCompressor implements Runnable {
    private final Path continuousDir;
    private final BiPredicate<Path, BasicFileAttributes> predicate;
    private final Comparator<Path> oldestFirst;

    public ContinuousAsyncProfilerCompressor(ContinuousAsyncProfilerNotManageablePropertiesRepository notManageablePropertiesRepository) {
        this.continuousDir = Paths.get(notManageablePropertiesRepository.geNotManageableProperties().getContinuousOutputDir());
        this.predicate = (p, ignore) -> p.getFileName().toString().endsWith("jfr");
        this.oldestFirst = (o1, o2) -> {
            long firstModified = o1.toFile().lastModified();
            long secondModified = o2.toFile().lastModified();
            return Long.compare(firstModified, secondModified);
        };
    }

    @Override
    public void run() {
        try (Stream<Path> pathStream = Files.find(continuousDir, 1, predicate)) {
            List<Path> notCompressedFiles = pathStream
                    .sorted(oldestFirst)
                    .collect(Collectors.toList());
            int counter = notCompressedFiles.size() - 2;
            for (Path source : notCompressedFiles) {
                if (counter <= 0) {
                    break;
                }
                Path target = Paths.get(source.toAbsolutePath().toString() + ".gz");
                log.info("Compressing: {}", source);
                compressGzip(source, target);
                Files.delete(source);
                counter--;
            }
        } catch (IOException e) {
            log.error("Some IO failed", e);
        }
    }

    public static void compressGzip(Path source, Path target) throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(target.toFile()))) {
            Files.copy(source, gos);
        }
    }
}

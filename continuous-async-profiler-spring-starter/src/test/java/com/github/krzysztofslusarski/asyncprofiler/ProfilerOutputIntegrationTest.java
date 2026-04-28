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

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = ProfilerOutputIntegrationTest.TestApp.class,
        properties = {
                "async-profiler.continuous.enabled=true",
                "async-profiler.continuous.dump-interval=1s",
                "async-profiler.continuous.compression-interval=0s",
                "async-profiler.continuous.output-dir.continuous=target/test-profiler-output/continuous",
                "async-profiler.continuous.output-dir.archive=target/test-profiler-output/archive"
        }
)
public class ProfilerOutputIntegrationTest {

    private static final Path OUTPUT_ROOT = Paths.get("target/test-profiler-output");
    private static final Path CONTINUOUS_DIR = OUTPUT_ROOT.resolve("continuous");

    @BeforeClass
    public static void cleanOutputDirBeforeBoot() throws Exception {
        deleteRecursively(OUTPUT_ROOT);
    }

    @AfterClass
    public static void cleanOutputDirAfter() throws Exception {
        deleteRecursively(OUTPUT_ROOT);
    }

    @Test
    public void shouldProduceJfrFilesInContinuousOutputDir() throws Exception {
        long deadline = System.currentTimeMillis() + 20_000L;
        List<Path> jfrFiles = Collections.emptyList();
        while (System.currentTimeMillis() < deadline) {
            jfrFiles = listJfrFiles();
            if (jfrFiles.stream().anyMatch(ProfilerOutputIntegrationTest::hasContent)) {
                return;
            }
            Thread.sleep(200L);
        }
        fail("No non-empty .jfr output files created in "
                + CONTINUOUS_DIR.toAbsolutePath() + ", saw: " + jfrFiles);
    }

    private static boolean hasContent(Path path) {
        try {
            return Files.isRegularFile(path) && Files.size(path) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<Path> listJfrFiles() throws Exception {
        if (!Files.isDirectory(CONTINUOUS_DIR)) {
            return Collections.emptyList();
        }
        try (Stream<Path> files = Files.list(CONTINUOUS_DIR)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".jfr"))
                    .collect(Collectors.toList());
        }
    }

    private static void deleteRecursively(Path root) throws Exception {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestApp {
    }
}

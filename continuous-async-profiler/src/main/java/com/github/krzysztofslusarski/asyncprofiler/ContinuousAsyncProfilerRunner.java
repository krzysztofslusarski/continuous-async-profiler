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
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.profiler.AsyncProfiler;

@Slf4j
@RequiredArgsConstructor
class ContinuousAsyncProfilerRunner implements Runnable {
    private final AsyncProfiler asyncProfiler;
    private final ContinuousAsyncProfilerProperties properties;
    private final ContinuousAsyncProfilerMBeanPropertiesService mBeanPropertiesService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    private boolean started = false;
    private String params = null;

    @Override
    public void run() {
        try {
            if (started) {
                log.info("Stopping async-profiler");
                asyncProfiler.execute("stop," + params);
                started = false;
            }

            if (stopFileExists()) {
                log.info("Stop file exists on filesystem: {}, will not run profiler", properties.getStopFile());
            } else if (disabledByMbean()) {
                log.info("Profiler stopped by MBean, will not run profiler");
            } else {
                log.info("Starting async-profiler");
                params = createParams();
                asyncProfiler.execute("start," + params);
                started = true;
            }
        } catch (IOException e) {
            log.error("Cannot run profiler", e);
            if (started) {
                try {
                    asyncProfiler.execute("stop," + params);
                } catch (IOException e2) {
                    log.error("Cannot stop profiler after first exception", e2);
                }
            }
        }
    }

    void shutdown() {
        log.info("Shutting down");
        asyncProfiler.stop();
    }

    private boolean stopFileExists() {
        return Paths.get(properties.getStopFile()).toFile().exists();
    }

    private boolean disabledByMbean() {
        return mBeanPropertiesService.getProperties().isDisableProfiler();
    }

    private String createParams() {
        String date = formatter.format(LocalDateTime.now());
        String event = mBeanPropertiesService.getProperties().getOverriddenEvent();
        if (event == null) {
            event = properties.getEvent();
        }

        return String.format(
                "jfr,event=%s,file=%s/%s-%s.jfr",
                event,
                properties.getContinuousOutputDir(),
                event,
                date
        );
    }
}

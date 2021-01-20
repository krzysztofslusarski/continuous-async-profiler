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
package com.github.krzysztofslusarski.asyncprofiler.mbean;

import com.github.krzysztofslusarski.asyncprofiler.ContinuousAsyncProfilerManageableProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ContinuousAsyncProfilerMBeanPropertiesService {
    private final ContinuousAsyncProfilerManageableProperties initialProperties;
    private volatile ContinuousAsyncProfilerManageableProperties properties;

    public ContinuousAsyncProfilerMBeanPropertiesService(ContinuousAsyncProfilerManageableProperties properties) {
        this.initialProperties = properties;
        this.properties = properties;
    }

    ContinuousAsyncProfilerManageableProperties getProperties() {
        return properties;
    }

    void setProperties(ContinuousAsyncProfilerManageableProperties properties) {
        log.info("Overriding properties with: {}", properties);
        this.properties = properties;
    }

    void resetToDefaults() {
        log.info("Reseting properties with: {}", initialProperties);
        this.properties = this.initialProperties;
    }
}

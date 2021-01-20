/*
 * Copyright 2020 Krzysztof Slusarski, Michal Rowicki
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
import com.github.krzysztofslusarski.asyncprofiler.ContinuousAsyncProfilerManageablePropertiesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Conditional(ContinuousAsyncProfilerMBeanCondition.class)
public class ContinuousAsyncProfilerMBeanConfiguration {
    private final ContinuousAsyncProfilerManageableProperties defaultManageableProperties;

    @Bean
    ContinuousAsyncProfilerMBeanPropertiesService continuousAsyncProfilerMBeanPropertiesService() {
        return new ContinuousAsyncProfilerMBeanPropertiesService(defaultManageableProperties);
    }

    @Bean
    ContinuousAsyncProfilerManageablePropertiesRepository continuousAsyncProfilerManageablePropertiesRepository(ContinuousAsyncProfilerMBeanPropertiesService continuousAsyncProfilerMBeanPropertiesService) {
        return new ContinuousAsyncProfilerMBeanManageablePropertiesRepository(continuousAsyncProfilerMBeanPropertiesService);
    }

    @Bean
    ContinuousAsyncProfilerMBean continuousAsyncProfilerMBean(ContinuousAsyncProfilerMBeanPropertiesService continuousAsyncProfilerMBeanPropertiesService) {
        return new ContinuousAsyncProfilerMBean(continuousAsyncProfilerMBeanPropertiesService);
    }
}

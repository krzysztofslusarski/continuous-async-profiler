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
package com.github.krzysztofslusarski.asyncprofiler;

import com.github.krzysztofslusarski.asyncprofiler.mbean.ContinuousAsyncProfilerMBeanConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ContinuousAsyncProfilerMBeanConfiguration.class)
@EnableConfigurationProperties(ContinuousAsyncProfilerBootProperties.class)
@Conditional(ContinuousAsyncProfilerCondition.class)
public class ContinuousAsyncProfilerAutoConfiguration {
    @Bean
    ContinuousAsyncProfilerManageableProperties defaultManageableProperties(ContinuousAsyncProfilerBootProperties properties) {
        return properties.toSpringFrameworkManageableProperties();
    }

    @Bean
    ContinuousAsyncProfilerNotManageableProperties defaultNotManageableProperties(ContinuousAsyncProfilerBootProperties properties) {
        return properties.toSpringFrameworkNotManageableProperties();
    }

    @Bean
    ContinuousAsyncProfiler continuousAsyncProfiler(ContinuousAsyncProfilerManageableProperties defaultManageableProperties,
                                                    ContinuousAsyncProfilerNotManageableProperties defaultNotManageableProperties,
                                                    @Autowired(required = false) ContinuousAsyncProfilerManageablePropertiesRepository manageablePropertiesRepository) {
        if (manageablePropertiesRepository == null) {
            manageablePropertiesRepository = () -> defaultManageableProperties;
        }

        return new ContinuousAsyncProfiler(manageablePropertiesRepository, defaultNotManageableProperties);
    }
}

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

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class ContinuousAsyncProfilerMBeanCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean continuousAsyncProfilerEnabled = isContinuousAsyncProfilerEnabled(conditionContext);
        boolean jmxEnabled = isJmxEnabled(conditionContext);
        return continuousAsyncProfilerEnabled && jmxEnabled;
    }

    private boolean isJmxEnabled(ConditionContext conditionContext) {
        String repositoryType = conditionContext.getEnvironment().getProperty("async-profiler.continuous.manageable-properties-repository");
        return "jmx".equalsIgnoreCase(repositoryType);
    }

    private boolean isContinuousAsyncProfilerEnabled(ConditionContext conditionContext) {
        String enabled = conditionContext.getEnvironment().getProperty("async-profiler.continuous.enabled");
        return Boolean.parseBoolean(enabled);
    }

}

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

import org.springframework.core.convert.converter.Converter;

import java.nio.file.Path;
import java.nio.file.Paths;

// Spring 4.x's default conversion service has no String -> java.nio.file.Path
// converter (added in Spring 5). Registered via @ConfigurationPropertiesBinding
// so Path-typed configuration properties (output-dir.*, profiler-lib-path,
// stop-work-file) can be overridden from application.properties on Spring Boot 1.5.
class ContinuousAsyncProfilerStringToPathConverter implements Converter<String, Path> {
    @Override
    public Path convert(String source) {
        return Paths.get(source);
    }
}

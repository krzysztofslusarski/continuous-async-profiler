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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Spring Boot 1.5 / Spring 4's default conversion service does not know
// how to convert String -> java.time.Duration. Registered via
// @ConfigurationPropertiesBinding so Duration-typed configuration
// properties accept both ISO-8601 ("PT1S") and shorthand ("60s").
class ContinuousAsyncProfilerStringToDurationConverter implements Converter<String, Duration> {
    private static final Pattern SIMPLE = Pattern.compile("^([+-]?\\d+)(ns|us|ms|s|m|h|d)?$");

    @Override
    public Duration convert(String source) {
        String value = source.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.charAt(0) == 'P'
                || (value.length() > 1 && value.charAt(0) == '-' && value.charAt(1) == 'P')) {
            return Duration.parse(value);
        }
        Matcher matcher = SIMPLE.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("'" + source + "' is not a valid duration");
        }
        long amount = Long.parseLong(matcher.group(1));
        return asDuration(amount, matcher.group(2));
    }

    private static Duration asDuration(long amount, String unit) {
        if (unit == null || "ms".equals(unit)) {
            return Duration.ofMillis(amount);
        }
        switch (unit) {
            case "ns": return Duration.ofNanos(amount);
            case "us": return Duration.of(amount, ChronoUnit.MICROS);
            case "s":  return Duration.ofSeconds(amount);
            case "m":  return Duration.ofMinutes(amount);
            case "h":  return Duration.ofHours(amount);
            case "d":  return Duration.ofDays(amount);
            default: throw new IllegalArgumentException("Unknown duration unit '" + unit + "'");
        }
    }
}

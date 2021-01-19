package com.github.krzysztofslusarski.asyncprofiler;

import lombok.Value;
import lombok.With;

@With
@Value
class ContinuousAsyncProfilerMBeanProperties {
    boolean disableProfiler;
    String overriddenEvent;
}

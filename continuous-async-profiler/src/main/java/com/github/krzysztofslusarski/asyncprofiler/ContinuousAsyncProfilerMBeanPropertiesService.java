package com.github.krzysztofslusarski.asyncprofiler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ContinuousAsyncProfilerMBeanPropertiesService {
    private volatile ContinuousAsyncProfilerMBeanProperties properties = new ContinuousAsyncProfilerMBeanProperties(false, null);

    ContinuousAsyncProfilerMBeanProperties getProperties() {
        return properties;
    }

    void setProperties(ContinuousAsyncProfilerMBeanProperties properties) {
        log.info("Overriding properties with: {}", properties);
        this.properties = properties;
    }
}

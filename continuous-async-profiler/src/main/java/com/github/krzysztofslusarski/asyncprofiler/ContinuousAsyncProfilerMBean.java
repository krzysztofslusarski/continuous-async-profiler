package com.github.krzysztofslusarski.asyncprofiler;

import lombok.RequiredArgsConstructor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@RequiredArgsConstructor
@ManagedResource("com.github.krzysztofslusarski:type=AsyncProfiler.Continuous")
public class ContinuousAsyncProfilerMBean {
    private final ContinuousAsyncProfilerMBeanPropertiesService propertiesService;

    @ManagedAttribute
    public boolean isDisabled() {
        return propertiesService.getProperties().isDisableProfiler();
    }

    @ManagedAttribute
    public String getOverriddenEvent() {
        return propertiesService.getProperties().getOverriddenEvent();
    }

    @ManagedOperation
    public void disable() {
        ContinuousAsyncProfilerMBeanProperties newProperties = propertiesService.getProperties().withDisableProfiler(true);
        propertiesService.setProperties(newProperties);
    }

    @ManagedOperation
    public void resume() {
        ContinuousAsyncProfilerMBeanProperties newProperties = propertiesService.getProperties().withDisableProfiler(false);
        propertiesService.setProperties(newProperties);
    }

    @ManagedOperation
    public void overrideEvent(String event) {
        ContinuousAsyncProfilerMBeanProperties newProperties = propertiesService.getProperties().withOverriddenEvent(event);
        propertiesService.setProperties(newProperties);
    }

    @ManagedOperation
    public void clearOverriddenEvent() {
        ContinuousAsyncProfilerMBeanProperties newProperties = propertiesService.getProperties().withOverriddenEvent(null);
        propertiesService.setProperties(newProperties);
    }
}

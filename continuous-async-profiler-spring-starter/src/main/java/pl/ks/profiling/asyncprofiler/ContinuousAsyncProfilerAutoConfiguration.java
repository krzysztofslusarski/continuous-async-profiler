package pl.ks.profiling.asyncprofiler;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ContinuousAsyncProfilerConfiguration.class)
public class ContinuousAsyncProfilerAutoConfiguration {
}

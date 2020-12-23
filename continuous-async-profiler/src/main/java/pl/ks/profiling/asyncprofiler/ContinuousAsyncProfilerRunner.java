package pl.ks.profiling.asyncprofiler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.profiler.AsyncProfiler;

@Slf4j
@RequiredArgsConstructor
class ContinuousAsyncProfilerRunner implements Runnable {
    private final AsyncProfiler asyncProfiler;
    private final ContinuousAsyncProfilerProperties properties;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        boolean started = false;
        String params = null;
        while (!Thread.interrupted()) {
            try {
                log.info("Starting async-profiler");
                params = createParams();
                asyncProfiler.execute("start," + params);
                started = true;
                Thread.sleep(properties.getDumpIntervalSeconds() * 1000);
                log.info("Stopping async-profiler");
                asyncProfiler.execute("stop," + params);
                started = false;
            } catch (IOException e) {
                log.error("Cannot run profiler", e);
            } catch (InterruptedException e) {
                return;
            } finally {
                if (started) {
                    try {
                        asyncProfiler.execute("stop," + params);
                    } catch (IOException e) {
                        log.error("Cannot stop profiler at finally", e);
                    }
                }
            }
        }
    }

    private String createParams() {
        String date = formatter.format(LocalDateTime.now());
        return String.format(
                "jfr,event=%s,file=%s/%s.jfr",
                properties.getEvent(),
                properties.getContinuousOutputDir(),
                date
        );
    }
}

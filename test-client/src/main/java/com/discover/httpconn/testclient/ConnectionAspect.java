package com.discover.httpconn.testclient;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ConnectionAspect {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionAspect.class);

    private final Timer connectionCreationTimer;

    public ConnectionAspect(MeterRegistry meterRegistry) {
        this.connectionCreationTimer =
                Timer.builder("http.connections.created.time")
                        .description("Time it took to create a new connection")
                        .register(meterRegistry);
    }

    @Around(
            "execution(* org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager.connect(..)))")
    public Object profileConnection(ProceedingJoinPoint proceedingJoinPoint) throws Exception {
        Object result =
                connectionCreationTimer.recordCallable(
                        () -> {
                            try {
                                return proceedingJoinPoint.proceed();
                            } catch (Throwable e) {
                                throw new RuntimeException(
                                        "Problem occured during timing of connection", e);
                            }
                        });
        if (LOG.isDebugEnabled()) {
            HistogramSnapshot snapshot = connectionCreationTimer.takeSnapshot();
            LOG.debug(
                    "Connection Timer Stats: count={}, total={}, mean={}, max={}",
                    snapshot.count(),
                    formatTime(snapshot.total(TimeUnit.MILLISECONDS)),
                    formatTime(snapshot.mean(TimeUnit.MILLISECONDS)),
                    formatTime(snapshot.max(TimeUnit.MILLISECONDS)));
        }
        return result;
    }

    private String formatTime(double time) {
        return new DecimalFormat("#.#").format(time) + "ms";
    }
}

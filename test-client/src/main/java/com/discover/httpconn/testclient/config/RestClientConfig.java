package com.discover.httpconn.testclient.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.PoolingHttpClientConnectionManagerMetricsBinder;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.RequestConfig.Builder;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    static final Logger LOG = LoggerFactory.getLogger(RestClientConfig.class);

    @Bean
    PoolingHttpClientConnectionManager connectionManager(
            HttpConnPoolProperties connPoolProperties) {
        LOG.info(
                "Creating PoolingHttpClientConnectionManager with properties: {}",
                connPoolProperties);

        ConnectionConfig connectionConfig =
                ConnectionConfig.custom()
                        .setConnectTimeout(connPoolProperties.connectTimeout(), TimeUnit.SECONDS)
                        .setSocketTimeout(connPoolProperties.socketTimeout(), TimeUnit.SECONDS)
                        .setTimeToLive(connPoolProperties.ttl(), TimeUnit.SECONDS)
                        .setValidateAfterInactivity(
                                connPoolProperties.inactiveValidationTime(), TimeUnit.SECONDS)
                        .build();

        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                // Options are FIFO and LIFO (default)
                .setConnPoolPolicy(
                        connPoolProperties.useFifo() ? PoolReusePolicy.FIFO : PoolReusePolicy.LIFO)
                // When only one host is being used, set the two below to same value
                .setMaxConnPerRoute(connPoolProperties.maxConnections())
                .setMaxConnTotal(connPoolProperties.maxConnections())
                // Options are LAX and STRICT (default)
                .setPoolConcurrencyPolicy(
                        connPoolProperties.useLaxConcurrency()
                                ? PoolConcurrencyPolicy.LAX
                                : PoolConcurrencyPolicy.STRICT)
                .build();
    }

    @Bean
    RestClient defaultRestClient(HttpConnPoolProperties connPoolProperties) {
        return RestClient.builder().baseUrl(connPoolProperties.baseUrl()).build();
    }

    @Bean
    RestClient restClient(
            HttpConnPoolProperties connPoolProperties,
            PoolingHttpClientConnectionManager connectionManager) {
        LOG.info("Creating RestClient with properties: {}", connPoolProperties);

        HttpClientBuilder httpClientBuilder =
                HttpClients.custom().setConnectionManager(connectionManager);

        Builder requestConfigBuilder = RequestConfig.custom();
        if (connPoolProperties.leaseRequestTimeout() > 0) {
            requestConfigBuilder.setConnectionRequestTimeout(
                    Timeout.ofSeconds(connPoolProperties.leaseRequestTimeout()));
        }
        if (connPoolProperties.maxIdleTime() > 0) {
            // this property doesn't actually send a keep alive to the server, just a property of
            // how long
            // the connection can remain usable in the pool when not active
            requestConfigBuilder.setConnectionKeepAlive(
                    TimeValue.ofSeconds(connPoolProperties.maxIdleTime()));
        }
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

        if (connPoolProperties.retryLimit() > 0 || connPoolProperties.retryWaitTimeMillis() > 0) {
            int retryLimit =
                    connPoolProperties.retryLimit() > 0 ? connPoolProperties.retryLimit() : 1;
            int retryWaitTimeMillis =
                    connPoolProperties.retryWaitTimeMillis() > 0
                            ? connPoolProperties.retryWaitTimeMillis()
                            : 1000;
            httpClientBuilder.setRetryStrategy(
                    new DefaultHttpRequestRetryStrategy(
                            retryLimit, TimeValue.ofMilliseconds(retryWaitTimeMillis)));
        }

        if (connPoolProperties.evictExpiredConnections()) {
            // Enables a background thread to proactively remove expired connections
            httpClientBuilder.evictExpiredConnections();
        }

        if (connPoolProperties.evictIdleConnections() && connPoolProperties.maxIdleTime() > 0) {
            // Enables a background thread to proactively evict idle connections exceeding the time
            // limit
            httpClientBuilder.evictIdleConnections(
                    TimeValue.ofSeconds(connPoolProperties.maxIdleTime()));
        }

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClientBuilder.build());

        return RestClient.builder()
                .baseUrl(connPoolProperties.baseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    PoolingHttpClientConnectionManagerMetricsBinder metrics(
            PoolingHttpClientConnectionManager connectionManager,
            MeterRegistry meterRegistry,
            HttpConnPoolProperties connPoolProperties) {

        PoolingHttpClientConnectionManagerMetricsBinder binder =
                new PoolingHttpClientConnectionManagerMetricsBinder(
                        connectionManager, connPoolProperties.name());
        binder.bindTo(meterRegistry);
        return binder;
    }
}

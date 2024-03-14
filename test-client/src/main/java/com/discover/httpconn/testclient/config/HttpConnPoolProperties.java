package com.discover.httpconn.testclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "http-client.connection-pool")
public record HttpConnPoolProperties(
        String baseUrl,
        String name,
        int maxConnections,
        boolean useFifo,
        boolean useLaxConcurrency,
        int ttl,
        int connectTimeout,
        int socketTimeout,
        int inactiveValidationTime,
        int maxIdleTime,
        int leaseRequestTimeout,
        boolean evictExpiredConnections,
        boolean evictIdleConnections,
        int retryLimit,
        int retryWaitTimeMillis) {}

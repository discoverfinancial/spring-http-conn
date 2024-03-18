package com.discover.httpconn.testclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.ConnectionProvider.Builder;

@Configuration
public class WebClientConfig {

    static final Logger LOG = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    WebClient defaultWebClient(HttpConnPoolProperties connPoolProps) {
        return WebClient.builder().baseUrl(connPoolProps.baseUrl()).build();
    }

    @Bean
    WebClient webClient(HttpConnPoolProperties connPoolProps, WebClient.Builder webClientBuilder) {
        LOG.info("Creating WebClient using connPoolProps={}", connPoolProps);
        Builder connProviderBuilder =
                ConnectionProvider.builder(connPoolProps.name())
                        .maxConnections(connPoolProps.maxConnections())
                        .maxIdleTime(Duration.ofSeconds(connPoolProps.maxIdleTime()))
                        .maxLifeTime(Duration.ofSeconds(connPoolProps.ttl()));

        if (connPoolProps.evictIdleConnections() && connPoolProps.maxIdleTime() > 0) {
            connProviderBuilder.evictInBackground(Duration.ofSeconds(connPoolProps.maxIdleTime()));
        }

        if (connPoolProps.useFifo()) {
            connProviderBuilder.fifo();
        } else {
            connProviderBuilder.lifo();
        }

        HttpClient httpClient =
                HttpClient.create(connProviderBuilder.build())
                        .metrics(
                                true,
                                uri ->
                                        uri.startsWith("/downstream/test/")
                                                ? "/downstream/test/{n}"
                                                : uri)
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                connPoolProps.connectTimeout() * 1000)
                        .doOnConnected(
                                connection ->
                                        connection
                                                .addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                connPoolProps.socketTimeout(),
                                                                TimeUnit.SECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                connPoolProps.socketTimeout(),
                                                                TimeUnit.SECONDS)));

        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(connPoolProps.baseUrl())
                .build();
    }
}

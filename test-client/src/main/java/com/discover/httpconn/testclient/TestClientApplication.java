package com.discover.httpconn.testclient;

import com.discover.httpconn.testclient.config.HttpConnPoolProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(HttpConnPoolProperties.class)
public class TestClientApplication {

    public static void main(String[] args) {
        // Disable DNS Caching
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
        SpringApplication.run(TestClientApplication.class, args);
    }
}

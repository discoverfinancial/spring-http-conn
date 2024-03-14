package com.discover.httpconn.testclient.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class WebClientController {
    static final Logger LOG = LoggerFactory.getLogger(WebClientController.class);

    private final WebClient webClient;
    private final WebClient defaultWebClient;

    public WebClientController(
            @Qualifier("webClient") WebClient webClient,
            @Qualifier("defaultWebClient") WebClient defaultWebClient) {
        this.webClient = webClient;
        this.defaultWebClient = defaultWebClient;
    }

    @GetMapping("/wc/test/{testnum}")
    public Response get(@PathVariable(name = "testnum") String testnum) {
        LOG.debug("get - Incoming request, testnum={}", testnum);
        return webClient
                .get()
                .uri("/downstream/test/" + testnum)
                .retrieve()
                .bodyToMono(Response.class)
                .block();
    }

    @GetMapping("/dwc/test/{testnum}")
    public Response defaultGet(@PathVariable(name = "testnum") String testnum) {
        LOG.debug("defaultGet - Incoming request, testnum={}", testnum);
        return defaultWebClient
                .get()
                .uri("/downstream/test/" + testnum)
                .retrieve()
                .bodyToMono(Response.class)
                .block();
    }

    @ExceptionHandler({Exception.class})
    public Response handleException() {
        return new Response("error");
    }
}

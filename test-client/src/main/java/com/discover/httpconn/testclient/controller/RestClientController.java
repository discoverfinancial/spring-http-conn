package com.discover.httpconn.testclient.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class RestClientController {
    static final Logger LOG = LoggerFactory.getLogger(RestClientController.class);

    private final RestClient restClient;
    private final RestClient defaultRestClient;

    public RestClientController(
            @Qualifier("restClient") RestClient restClient,
            @Qualifier("defaultRestClient") RestClient defaultRestClient) {
        this.restClient = restClient;
        this.defaultRestClient = defaultRestClient;
    }

    @GetMapping("/rc/test/{testnum}")
    public Response get(@PathVariable(name = "testnum") String testnum) {
        // LOG.debug("get - testnum={}", testnum);
        return restClient.get().uri("/downstream/test/" + testnum).retrieve().body(Response.class);
    }

    @GetMapping("/drc/test/{testnum}")
    public Response defaultGet(@PathVariable(name = "testnum") String testnum) {
        // LOG.debug("defaultGet - testnum={}", testnum);
        return defaultRestClient
                .get()
                .uri("/downstream/test/" + testnum)
                .retrieve()
                .body(Response.class);
    }
}

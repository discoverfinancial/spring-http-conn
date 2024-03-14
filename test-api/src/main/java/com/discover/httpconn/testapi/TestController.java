package com.discover.httpconn.testapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	static final Logger LOG = LoggerFactory.getLogger(TestController.class);

	private final String location;
	private final boolean connectionClose;

	public TestController(@Value("${location:notset}") String location,
			@Value("${connection-close:false}") boolean connectionClose) {
		this.location = location;
		this.connectionClose = connectionClose;
		LOG.info("Created TestController with location={} and connectionClose={}", location, connectionClose);
	}

	@GetMapping("/downstream/test/{testnum}")
	public ResponseEntity<Response> downstream(@PathVariable(name = "testnum") long testnum) {
		LOG.debug("Incoming Request with testnum={}", testnum);
		if (testnum > 0) {
			sleep(testnum);
		}
		BodyBuilder responseBuilder = ResponseEntity.ok();
		if (connectionClose) {
			responseBuilder.header(HttpHeaders.CONNECTION, "Close");
		}
		return responseBuilder.body(new Response("Response from " + location));
	}

	private void sleep(long testnum) {
		try {
			Thread.sleep(Math.min(testnum, 30L) * 1000L);
		} catch (Exception e) {
			LOG.error("Problem sleeping", e);
		}
	}
}

record Response(String message) {
}
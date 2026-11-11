package com.zxcv.springbootspringmvc.module.restclient.web;

import com.zxcv.springbootspringmvc.common.httpclient.HttpApiClient;
import com.zxcv.springbootspringmvc.module.restclient.dto.PostTestRequest;
import com.zxcv.springbootspringmvc.module.restclient.dto.PostTestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest-client")
@RequiredArgsConstructor
public class RestClientController {

    @Value("${app.clients.mock-service.base-url}")
    private String baseUrl;

    private final HttpApiClient httpApiClient;

    @GetMapping("/get-test")
    public String getTest() {
        var name = "John Doe";
        var message = "Hello!";
        return httpApiClient.get(
                baseUrl + "/personal/test/get-test/{name}?message={message}",
                new ParameterizedTypeReference<>() {
                },
                name,
                message
        );
    }

    @PostMapping("/post-test")
    public PostTestResponse postTest(@RequestBody PostTestRequest body) {
        return httpApiClient.post(
                baseUrl + "/personal/test/post-test",
                body,
                new ParameterizedTypeReference<>() {
                }
        );
    }

}

package com.zxcv.springbootspringmvc.common.httpclient;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class HttpApiClient {

    private final RestClient restClient;

    public <T> T get(String uri, ParameterizedTypeReference<T> responseType, Object... uriVariables) {
        return get(uri, headers -> {
        }, responseType, uriVariables);
    }

    public <T> T get(
            String uri,
            Consumer<HttpHeaders> headers,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables
    ) {
        return restClient.get()
                .uri(uri, uriVariables)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(responseType);
    }

    public <T> T post(String uri, Object body, ParameterizedTypeReference<T> responseType, Object... uriVariables) {
        return post(uri, headers -> {
        }, body, responseType, uriVariables);
    }

    public <T> T post(
            String uri,
            Consumer<HttpHeaders> headers,
            Object body,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables
    ) {
        return restClient.post()
                .uri(uri, uriVariables)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(responseType);
    }

    // use this will not throw any kind of http status as long as it's not timeout
    public <T> ResponseEntity<T> getEntityAnyStatus(
            String uri,
            Consumer<HttpHeaders> headers,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables
    ) {
        return restClient.get()
                .uri(uri, uriVariables)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                })
                .toEntity(responseType);
    }

    // use this will not throw any kind of http status as long as it's not timeout
    public <T> ResponseEntity<T> postEntityAnyStatus(
            String uri,
            Consumer<HttpHeaders> headers,
            Object body,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables
    ) {
        return restClient.post()
                .uri(uri, uriVariables)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                })
                .toEntity(responseType);
    }

}

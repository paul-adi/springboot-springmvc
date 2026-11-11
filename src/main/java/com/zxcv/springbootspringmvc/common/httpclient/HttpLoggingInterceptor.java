package com.zxcv.springbootspringmvc.common.httpclient;

import com.zxcv.springbootspringmvc.common.logging.HttpLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        long start = System.nanoTime();
        log.info("[HTTP CLIENT OUT] {} {} -> Request started", request.getMethod(), request.getURI());

        if (log.isDebugEnabled()) {
            log.debug(
                    "[HTTP CLIENT OUT] {} {} -> Request headers : {}. Request body : {}",
                    request.getMethod(),
                    request.getURI(),
                    headersToJson(request.getHeaders()),
                    HttpLogUtils.bodyForLog(body, contentTypeOf(request.getHeaders()))
            );
        }

        try {
            var response = execution.execute(request, body);

            log.info(
                    "[HTTP CLIENT OUT] {} {} -> Request completed with status : {} ({} ms)",
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode().value(),
                    HttpLogUtils.elapsedMillis(start)
            );

            if (log.isDebugEnabled()) {
                InputStream original = response.getBody();
                byte[] loggedBytes = original.readNBytes(HttpLogUtils.MAX_LOGGED_BODY_BYTES + 1);
                log.debug(
                        "[HTTP CLIENT OUT] {} {} -> Response headers : {}. Response body : {}",
                        request.getMethod(),
                        request.getURI(),
                        headersToJson(response.getHeaders()),
                        HttpLogUtils.bodyForLog(loggedBytes, contentTypeOf(response.getHeaders()))
                );

                return new ReplayedResponse(
                        response, new SequenceInputStream(new ByteArrayInputStream(loggedBytes), original)
                );
            }

            return response;
        } catch (Throwable t) {
            log.warn(
                    "[HTTP CLIENT OUT] {} {} -> Request failed after {} ms. {}",
                    request.getMethod(),
                    request.getURI(),
                    HttpLogUtils.elapsedMillis(start),
                    t.toString()
            );
            throw t;
        }
    }

    private static String headersToJson(HttpHeaders headers) {
        Map<String, List<String>> headersMap = new LinkedHashMap<>();
        headers.forEach(headersMap::put);
        return JSON_MAPPER.writeValueAsString(headersMap);
    }

    private static String contentTypeOf(HttpHeaders headers) {
        MediaType type = headers.getContentType();
        return type != null ? type.toString() : null;
    }

    private record ReplayedResponse(ClientHttpResponse delegate, InputStream body) implements ClientHttpResponse {

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public void close() {
            delegate.close();
        }
    }

}

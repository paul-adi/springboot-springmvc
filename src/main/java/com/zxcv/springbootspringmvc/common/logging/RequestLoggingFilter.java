package com.zxcv.springbootspringmvc.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
// Order is set after ServerHttpObservationFilter (traceId will be included),
// and before Spring Security (rejected requests 401/403 will be logged)
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!log.isInfoEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        boolean debug = log.isDebugEnabled();
        HttpServletRequest req = debug
                ? new ContentCachingRequestWrapper(request, HttpLogUtils.MAX_LOGGED_BODY_BYTES + 1)
                : request;
        HttpServletResponse res = debug ? new ContentCachingResponseWrapper(response) : response;

        String method = request.getMethod();
        String path = request.getQueryString() == null
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + request.getQueryString();

        long start = System.nanoTime();
        try {
            chain.doFilter(req, res);
            log.info(
                    "[HTTP SERVER IN] {} {} -> Request completed with status : {} ({} ms)",
                    method,
                    path,
                    res.getStatus(),
                    HttpLogUtils.elapsedMillis(start)
            );
        } catch (Throwable t) {
            log.warn(
                    "[HTTP SERVER IN] {} {} -> Request failed after {} ms. {}",
                    method,
                    path,
                    HttpLogUtils.elapsedMillis(start),
                    t.toString()
            );
            throw t;
        } finally {
            if (debug) {
                var cachedRequest = (ContentCachingRequestWrapper) req;
                var cachedResponse = (ContentCachingResponseWrapper) res;

                log.debug(
                        "[HTTP SERVER IN] {} {} -> Request headers : {}. Request body : {}",
                        method,
                        path,
                        requestHeaders(request),
                        HttpLogUtils.bodyForLog(cachedRequest.getContentAsByteArray(), request.getContentType())
                );
                log.debug(
                        "[HTTP SERVER IN] {} {} -> Response body : {}",
                        method,
                        path,
                        HttpLogUtils.bodyForLog(
                                cachedResponse.getContentAsByteArray(), cachedResponse.getContentType()
                        )
                );

                cachedResponse.copyBodyToResponse();
            }
        }
    }

    private static String requestHeaders(HttpServletRequest request) {
        Map<String, List<String>> headersMap = new LinkedHashMap<>();
        request.getHeaderNames().asIterator()
                .forEachRemaining(name -> headersMap.put(name, Collections.list(request.getHeaders(name))));
        return JSON_MAPPER.writeValueAsString(headersMap);
    }

}

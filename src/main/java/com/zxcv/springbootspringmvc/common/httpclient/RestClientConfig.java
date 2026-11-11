package com.zxcv.springbootspringmvc.common.httpclient;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class RestClientConfig {

    @Bean
    public CloseableHttpClient httpClient(RestClientProperties properties) {
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(properties.pool().maxTotalConnections())
                .setMaxConnPerRoute(properties.pool().maxConnectionsPerRoute())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.of(properties.connectTimeout()))
                        .setTimeToLive(TimeValue.of(properties.pool().timeToLive()))
                        .build())
                .useSystemProperties()
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictIdleConnections(TimeValue.of(properties.pool().idleTimeout()))
                .evictExpiredConnections()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.of(properties.connectionRequestTimeout()))
                        .setResponseTimeout(Timeout.of(properties.responseTimeout()))
                        .build())
                .build();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer(CloseableHttpClient httpClient) {
        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        var loggingInterceptor = new HttpLoggingInterceptor();

        return builder -> builder
                .requestFactory(requestFactory)
                .requestInterceptor(loggingInterceptor);
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

}

package com.zxcv.springbootspringmvc.common.httpclient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.rest-client")
public record RestClientProperties(
        @Valid @NotNull Pool pool,
        @NotNull Duration connectTimeout,
        @NotNull Duration connectionRequestTimeout,
        @NotNull Duration responseTimeout
) {

    public record Pool(
            @Positive int maxTotalConnections,
            @Positive int maxConnectionsPerRoute,
            @NotNull Duration idleTimeout,
            @NotNull Duration timeToLive
    ) {
    }

}

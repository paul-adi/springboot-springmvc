package com.zxcv.springbootspringmvc.module.restclient.dto;

import lombok.Builder;

@Builder
public record PostTestResponse(
        String status,
        String message
) {
}

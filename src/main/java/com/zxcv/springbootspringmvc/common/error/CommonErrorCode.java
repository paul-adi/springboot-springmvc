package com.zxcv.springbootspringmvc.common.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "GEN-400", "Invalid request data"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-500", "Something went wrong, please try again");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

}

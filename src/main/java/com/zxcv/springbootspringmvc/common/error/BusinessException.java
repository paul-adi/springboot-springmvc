package com.zxcv.springbootspringmvc.common.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.code() + " " + errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.code() + " " + errorCode.message(), cause);
        this.errorCode = errorCode;
    }

}

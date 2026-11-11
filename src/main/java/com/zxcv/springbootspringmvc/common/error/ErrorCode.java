package com.zxcv.springbootspringmvc.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus status();

    String code();

    String message();

}

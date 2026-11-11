package com.zxcv.springbootspringmvc.common.async;

import com.zxcv.springbootspringmvc.common.logging.ExceptionLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async error. Method : {}. Error : {}", method.getName(), ExceptionLogUtils.summarize(ex));
    }

}

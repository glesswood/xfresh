// common/GlobalExceptionHandler.java
package com.xfresh.common;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBiz(BusinessException ex) {
        return ApiResponse.of(ex.getCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOthers(Exception ex) {
        // TODO 打日志
        return ApiResponse.of(500, ex.getMessage(), null);
    }
}
package com.xfresh.order.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// common/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StockException.class)
    public ApiResp<Void> stock(StockException e){ return ApiResp.fail(e.getMessage(), 1001); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResp<Void> bad(MethodArgumentNotValidException e){
        String m = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResp.fail(m, 1000);
    }
    @ExceptionHandler(Exception.class)
    public ApiResp<Void> other(Exception e){
        return ApiResp.fail("internal_error", 9999);
    }
}
package com.xfresh.exception;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class DuplicateRequestException extends BusinessException {
    public DuplicateRequestException(String msg) { super(409, msg); }
}
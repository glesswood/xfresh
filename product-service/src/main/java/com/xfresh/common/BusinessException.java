// common/BusinessException.java
package com.xfresh.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(int code, String msg) { super(msg); this.code = code; }
    public BusinessException(String msg) { this(1, msg); }
}
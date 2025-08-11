// common/ApiResponse.java
package com.xfresh.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor(staticName = "of")
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return of(0, "success", data);
    }
    public static ApiResponse<Void> ok() {
        return of(0, "success", null);
    }
    public static ApiResponse<Void> fail(String msg) {
        return of(1, msg, null);
    }
}
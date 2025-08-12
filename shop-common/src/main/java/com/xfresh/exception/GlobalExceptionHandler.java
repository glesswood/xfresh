// common/GlobalExceptionHandler.java
package com.xfresh.exception;
import com.xfresh.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
// shop-common/src/main/java/com/xfresh/common/GlobalExceptionHandler.java

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestControllerAdvice(basePackages = "com.xfresh")  // 所有引入 common 的服务都会生效（见第3点）
public class GlobalExceptionHandler {

    /** 业务异常：返回 200，body 中体现失败（看你现在的 ApiResponse 设计习惯） */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBiz(BusinessException e) {
        return ApiResponse.of(e.getCode(), e.getMessage(), null);
    }

    /** 参数校验：@Valid/@Validated 触发 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInvalid(MethodArgumentNotValidException e) {
        var msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .findFirst().orElse("参数不合法");
        return ApiResponse.fail(msg);
    }

    /** 单参数校验异常（如 @RequestParam 校验） */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException e) {
        return ApiResponse.fail(e.getMessage());
    }

    /** 兜底：避免 500 裸奔（仅开发期可打印堆栈） */
    /*@ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleOther(Exception e) {
        // log.error("Unhandled ex", e);
        return ApiResponse.fail("服务器开小差了，请稍后再试");
    }*/
    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateRequestException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.of(409, e.getMessage(), null));
    }
}
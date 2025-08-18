package com.xfresh.stock.domain.exception;

/**
 * 库存领域统一异常。
 *
 * <p>常见使用场景：
 * <ul>
 *   <li>库存不足（锁定失败）</li>
 *   <li>回滚 / 解锁失败</li>
 *   <li>Redis / DB 状态不一致</li>
 * </ul>
 *
 * <p>抛出后由 {@code @RestControllerAdvice} 统一拦截，
 * 返回友好的 JSON 结构，例如：
 * <pre>
 * {
 *   "code": 409,
 *   "message": "库存不足，商品ID=10086"
 * }
 * </pre>
 */
public class StockException extends RuntimeException {

    /** 业务自定义错误码，方便前端做区分（可选） */
    private final int code;

    public StockException(String message) {
        super(message);
        this.code = 409;          // 默认冲突/库存不足
    }

    public StockException(int code, String message) {
        super(message);
        this.code = code;
    }

    public StockException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
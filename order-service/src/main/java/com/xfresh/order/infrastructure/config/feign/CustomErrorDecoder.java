package com.xfresh.order.infrastructure.config.feign;

import com.xfresh.exception.BusinessException;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        // 4xx -> 业务异常（不重试）
        if (status >= 400 && status < 500) {
            String msg = "远程调用参数/权限错误（" + status + "）";
            return new BusinessException(msg);
        }

        // 5xx -> 转换为 RetryableException（可被 Resilience4j Retry 捕获重试）
        if (status >= 500) {
            Request req = response.request();
            return new RetryableException(
                    status,
                    "远程服务异常（" + status + "）",
                    req != null ? req.httpMethod() : null,
                    new Date(),
                    req);
        }

        // 其他交给默认
        return defaultDecoder.decode(methodKey, response);
    }
}
package com.xfresh.order.config.feign;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Retryer feignRetryer() {
        // 和 yml 里的 NeverRetry 二选一：
        // 这里返回 NeverRetry，实际重试交给 Resilience4j
        return Retryer.NEVER_RETRY;
    }
}
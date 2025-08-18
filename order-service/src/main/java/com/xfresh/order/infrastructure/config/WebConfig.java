package com.xfresh.order.infrastructure.config;


import com.xfresh.order.application.idempotency.IdempotencyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final IdempotencyInterceptor idem;

    public WebConfig(IdempotencyInterceptor idem) {
        this.idem = idem;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idem)
                .addPathPatterns("/api/orders/**"); // 只拦下单接口
    }
}
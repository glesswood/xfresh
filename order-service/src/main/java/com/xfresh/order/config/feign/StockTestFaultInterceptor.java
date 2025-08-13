package com.xfresh.order.config.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// order-service: com.xfresh.order.config.feign.StockTestFaultInterceptor
@Configuration
public class StockTestFaultInterceptor {

    @Bean
    public feign.RequestInterceptor injectTestFaultParams() {
        return template -> {
            var attrs = (org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            var req = attrs.getRequest();

            String delay = req.getHeader("X-Delay");           // 如：2000
            String force500 = req.getHeader("X-Error500");     // "true" / "false"
            if (delay != null && !delay.isBlank()) {
                template.query("delay", delay);
            }
            if ("true".equalsIgnoreCase(force500)) {
                template.query("force500", "true");
            }
        };
    }
}
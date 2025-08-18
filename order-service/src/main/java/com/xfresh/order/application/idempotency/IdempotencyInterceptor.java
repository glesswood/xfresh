package com.xfresh.order.application.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedis;

    public IdempotencyInterceptor(StringRedisTemplate stringRedis) {
        this.stringRedis = stringRedis;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String key = req.getHeader("Idempotency-Key"); // 或从 body 里取
        if (key == null || key.isBlank()) return true; // 没带就放行（也可以直接拒绝）

        String redisKey = "idem:order:create:" + key;
        Boolean ok = stringRedis.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(ok)) { // 已存在，判定重复
            resp.setStatus(409);
            return false;
        }
        return true;
    }
}
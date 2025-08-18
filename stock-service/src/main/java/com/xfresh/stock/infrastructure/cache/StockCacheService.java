package com.xfresh.stock.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StockCacheService {

    private final StringRedisTemplate redis;

    private static final String LUA_TRY_RESERVE = """
    local a = tonumber(redis.call('GET', KEYS[1]) or '0')
    local q = tonumber(ARGV[1])
    if a < q then return 0 end
    redis.call('DECRBY', KEYS[1], q)
    return 1
    """;

    private static final String LUA_RELEASE = """
    redis.call('INCRBY', KEYS[1], tonumber(ARGV[1]))
    return 1
    """;

    private final DefaultRedisScript<Long> tryReserveScript = new DefaultRedisScript<>(LUA_TRY_RESERVE, Long.class);
    private final DefaultRedisScript<Long> releaseScript = new DefaultRedisScript<>(LUA_RELEASE, Long.class);

    private String key(Long pid) { return "stock:avail:" + pid; }

    public void warmIfAbsent(Long pid, int avail) {
        redis.opsForValue().setIfAbsent(key(pid), String.valueOf(avail));
        // 如需 TTL：redis.expire(key(pid), java.time.Duration.ofMinutes(30));
    }

    /** 预扣：可用>=qty 则 DECRBY；否则返回 false */
    public boolean tryReserve(Long pid, int qty) {
        List<String> keys = Collections.singletonList(key(pid));
        Long ok = redis.execute(tryReserveScript, keys, String.valueOf(qty));
        return ok != null && ok == 1L;
    }

    /** 释放/补偿：INCRBY qty */
    public void release(Long pid, int qty) {
        List<String> keys = Collections.singletonList(key(pid));
        redis.execute(releaseScript, keys, String.valueOf(qty));
    }

    /** （可选）校正缓存：确认/回滚后用 DB 值覆盖 */
    public void setAvail(Long pid, int avail) {
        redis.opsForValue().set(key(pid), String.valueOf(avail));
    }
}
package com.xfresh.stock.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /** ① 通用 RedisTemplate，Key=String，Value=JSON */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);
        // key/hashKey 用字符串序列化器
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setHashKeySerializer(new StringRedisSerializer());
        // value/hashValue 用 JSON
        tpl.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        tpl.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        tpl.afterPropertiesSet();
        return tpl;
    }

    /** ② Lua 脚本 Bean（返回 Long） */
    @Bean
    public DefaultRedisScript<Long> stockLuaScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/stock.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory cf) {
        return new StringRedisTemplate(cf);   // 默认全部 StringSerializer
    }
}
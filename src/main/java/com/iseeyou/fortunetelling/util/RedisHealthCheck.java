package com.iseeyou.fortunetelling.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisHealthCheck {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthCheck(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void checkConnection() {
        try {
            String result = redisTemplate.execute((RedisConnection connection) -> connection.ping());
            log.info("Redis connection test: {}", result);
            if ("PONG".equalsIgnoreCase(result)) {
                log.info("Redis is connected and working properly");
            }
        } catch (Exception e) {
            log.error("Redis connection failed: {}", e.getMessage(), e);
        }
    }
}
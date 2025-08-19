package com.iseeyou.fortunetelling.controller.debug;

import com.iseeyou.fortunetelling.repository.JwtTokenRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/debug")
@Tag(name = "Debug", description = "Debug API, for internal use only")
public class RedisDebugController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenRepository jwtTokenRepository;

    public RedisDebugController(RedisTemplate<String, Object> redisTemplate, JwtTokenRepository jwtTokenRepository) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenRepository = jwtTokenRepository;
    }

    @GetMapping("/redis/keys")
    public Map<String, Object> checkRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            Set<String> keys = redisTemplate.keys("jwtToken:*");
            result.put("keys", keys);
            result.put("count", keys.size());
            result.put("connected", true);

            // Count tokens in repository
            long count = StreamSupport.stream(jwtTokenRepository.findAll().spliterator(), false).count();
            result.put("repositoryCount", count);

            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("connected", false);
            return result;
        }
    }
}
package com.rate.limit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private static final String LUA = """
            local c = redis.call('INCR', KEYS[1])
            if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end
            local ttl = redis.call('TTL', KEYS[1])
            return {c, ttl}
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script = new DefaultRedisScript<>(LUA, List.class);

    @Value("${rate-limit.max-request}")
    private int maxRequest;

    @Value("${rate-limit.window-seconds}")
    private long windowSeconds;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public RateLimitResult checkRateLimit(String identifier) {
        String key = "rate_limit:" + identifier;
        try {
            List<Long> res = (List<Long>) redisTemplate.execute(
                    script, List.of(key), String.valueOf(windowSeconds));

            int count = res.get(0).intValue();
            long ttl = Math.max(res.get(1), 1);
            int remaining = Math.max(0, maxRequest - count);
            boolean allowed = count <= maxRequest;

            return new RateLimitResult(allowed, count, remaining, maxRequest, ttl);
        } catch (Exception e) {
            // Fail open: if Redis is down, don't take the whole API down
            log.error("Redis unavailable, allowing request for {}", identifier, e);
            return new RateLimitResult(true, 0, maxRequest, maxRequest, 0);
        }
    }
}
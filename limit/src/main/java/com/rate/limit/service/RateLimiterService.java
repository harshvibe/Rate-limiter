package com.rate.limit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private static final String LUA = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2]) * 1000
            local limit = tonumber(ARGV[3])
            local member = ARGV[4]

            redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
            local count = redis.call('ZCARD', key)

            if count < limit then
                redis.call('ZADD', key, now, member)
                redis.call('PEXPIRE', key, window)
                return {1, count + 1, 0}
            else
                local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                local retry = math.ceil((tonumber(oldest[2]) + window - now) / 1000)
                return {0, count, retry}
            end
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
        long now = System.currentTimeMillis();
        String member = now + "-" + UUID.randomUUID();
        try {
            List<Long> res = (List<Long>) redisTemplate.execute(
                    script, List.of(key),
                    String.valueOf(now),
                    String.valueOf(windowSeconds),
                    String.valueOf(maxRequest),
                    member);

            boolean allowed = res.get(0) == 1L;
            int count = res.get(1).intValue();
            long retryAfter = allowed ? 0 : Math.max(res.get(2), 1L);

            return new RateLimitResult(allowed, count, Math.max(0, maxRequest - count),
                    maxRequest, retryAfter);
        } catch (Exception e) {
            // Fail open: if Redis is down, don't take the whole API down
            log.error("Redis unavailable, allowing request for {}", identifier, e);
            return new RateLimitResult(true, 0, maxRequest, maxRequest, 0);
        }
    }
}
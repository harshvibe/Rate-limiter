package com.rate.limit.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private RedisTemplate<String,String> redisTemplate;

    @Value("${rate-limit.max-request}")
    private  int maxRequest;

    @Value("${rate-limit.window-seconds}")
    private  long windowSeconds;

    public RateLimiterService(RedisTemplate<String,String> redisTemplate){
        this.redisTemplate=redisTemplate;
    }
    public RateLimitResult checkRateLimit(String identifier){
        String key="rate limit"+identifier;
        Long count=redisTemplate.opsForValue().increment(key);

        if(count==1){
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        int currentCount=count.intValue();
        int remaining=Math.max(0,(maxRequest-currentCount));
        boolean allowed=currentCount<=maxRequest;

        return new RateLimitResult(
                allowed,currentCount,remaining,maxRequest
        );
    }


}

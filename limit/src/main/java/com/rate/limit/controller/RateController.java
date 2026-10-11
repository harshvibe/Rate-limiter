package com.rate.limit.controller;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
public class RateController {

    private final StringRedisTemplate redisTemplate;

    public RateController(StringRedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    @PostMapping
    public String redisTest() {
        redisTemplate.opsForValue().set("test","hello");
        return "value stored in redis";
    }

    @GetMapping
    public String getTest() {
        return redisTemplate.opsForValue().get("test");
    }

}

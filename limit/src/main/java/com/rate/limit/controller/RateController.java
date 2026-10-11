package com.rate.limit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
public class RateController {

    private RedisTemplate<String, String> redisTemplate;

    public RateController(RedisTemplate redisTemplate){
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

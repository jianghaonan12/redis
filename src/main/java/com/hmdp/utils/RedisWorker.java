package com.hmdp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisWorker {
    private static final long BEGIN_TIME = 1672531200L;
    private RedisTemplate redisTemplate;
    public RedisWorker(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    public long nextId(String keyPrefix) {
        LocalDateTime dateTime = LocalDateTime.now();
        long nowSecond = dateTime.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIME;
        LocalDate now = LocalDate.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = redisTemplate.opsForValue().increment(keyPrefix + date);
        return timestamp << 32 | count;
    }




}

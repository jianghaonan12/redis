package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.221.128:6379");
        return Redisson.create(config);
    }
    @Bean
    public RedissonClient redissonClient2(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.221.128:6380");
        return Redisson.create(config);
    }
}

package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@Slf4j
public class CacheClient {
    @Autowired
    private StringRedisTemplate redisTemplate;
    //存入redis
    public void set(String key, Object value, Long time, TimeUnit unit){
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }
    //设置过期时间
    public void setWithLogicalExpire(String key, Object value, Long time ){
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(time));
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
    }
    //缓存穿透
    public <R,T> R queryWithPassThrough(String keyPrefix, T id, Class<R> entity, Function<T,R> dbFallback){
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)){
            R r = JSONUtil.toBean(json, entity);
            return r;
        }
        if (json != null){
            return null;
        }
        R r = dbFallback.apply(id);
        if (BeanUtil.isEmpty(r)){
            redisTemplate.opsForValue().set(key,"",EnumConstants.SHOP_NULL.getKey(),TimeUnit.MINUTES);
        }
        String jsonStr = JSONUtil.toJsonStr(r);
        redisTemplate.opsForValue().set(key,jsonStr,EnumConstants.SHOP.getKey(), TimeUnit.MINUTES);
        return r;
    }
    //缓存击穿
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    public <T,R>R queryWithLogicalExpire(T id,String keyPrefix,Class<R> entity,Long time,Function<T,R> dbFallBack){
        String key = keyPrefix+id;
        String hot = redisTemplate.opsForValue().get(key);
        RedisData redisData = JSONUtil.toBean(hot, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        R bean = JSONUtil.toBean((JSONObject) redisData.getData(), entity);
        if (expireTime.isAfter(LocalDateTime.now())){
            return bean;
        }
        String lockKey = EnumConstants.SHOP.getValue()+"lock";
        boolean flag = tryLock(lockKey);
        if (!flag){
            return bean;
        }
        if (expireTime.isAfter(LocalDateTime.now())){
            return bean;
        }
        CACHE_REBUILD_EXECUTOR.submit(()->{
            try {
                R db = dbFallBack.apply(id);
                this.setWithLogicalExpire(key,db,time);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                unLock(lockKey);
            }
        });
        return bean;
    }

    public boolean tryLock(String key){
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    public void unLock(String key){
        redisTemplate.delete(key);
    }

}

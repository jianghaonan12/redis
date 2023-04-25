package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock{
    private StringRedisTemplate redisTemplate;
    private String name;
    private static final String LOCK_PREFIX = "lock:";
    private static final String THREAD_PREFIX = UUID.randomUUID().toString(true)+"-";
    private static final DefaultRedisScript<Long> UNCLOCK_SCRIPT;
    static {
        UNCLOCK_SCRIPT = new DefaultRedisScript<>();
        UNCLOCK_SCRIPT.setLocation(new ClassPathResource("Unlock.lua"));
        UNCLOCK_SCRIPT.setResultType(Long.class);
    }

    public SimpleRedisLock(StringRedisTemplate redisTemplate, String name) {
        this.redisTemplate = redisTemplate;
        this.name = name;
    }
    @Override
    public boolean tryLock(long timeout) {
        long id = Thread.currentThread().getId();
        Boolean isSuccess = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX+name, THREAD_PREFIX+id, timeout, TimeUnit.MINUTES);
        boolean isTrue = BooleanUtil.isTrue(isSuccess);
        return isTrue;
    }

    @Override
    public void unlock() {
        ArrayList<String> keys = new ArrayList();
        keys.add(LOCK_PREFIX+name);
        redisTemplate.execute(UNCLOCK_SCRIPT,keys,THREAD_PREFIX+Thread.currentThread().getId());
    }
}

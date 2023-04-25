package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.config.RedisConfig;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.EnumConstants;
import com.hmdp.utils.RedisData;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CacheClient client;
    @Override
    public Result getShopById(Long id) {
//        String keyPrefix = EnumConstants.SHOP.getValue();
//        Shop shop = client.queryWithPassThrough(keyPrefix, id, Shop.class, this::getById);
        String keyPrefix = EnumConstants.SHOP.getValue();
        Shop shop = client.queryWithLogicalExpire(id, keyPrefix, Shop.class, 30L, this::getById);
        return Result.ok(shop);
    }

    public void save2Redis(Long id) throws InterruptedException {
        RedisData redisData = new RedisData();
        Shop shop = getById(id);
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(5));
        redisTemplate.opsForValue().set(EnumConstants.SHOP.getValue()+id,JSONUtil.toJsonStr(redisData));
    }
    @Override
    @Transactional(rollbackFor = Exception.class )
    public Result updateShop(Shop shop) {
        if (shop.getId()==null){
            throw new RuntimeException("该商铺不存在");
        }
        updateById(shop);
        redisTemplate.delete(EnumConstants.SHOP.getValue()+shop.getId().toString());
        return Result.ok();
    }
}

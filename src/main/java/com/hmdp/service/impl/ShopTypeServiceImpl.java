package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.EnumConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

        @Autowired
        private StringRedisTemplate redisTemplate;
    @Override
    public Result typeList() {
        String shopTypeStr = redisTemplate.opsForValue().get(EnumConstants.SHOP_TYPE.getValue());
        if (StrUtil.isNotBlank(shopTypeStr)){
            List<ShopType> shopTypeList = JSONUtil.toList(shopTypeStr, ShopType.class);
            return Result.ok(shopTypeList);
        }
        List<ShopType> shopTypeList = lambdaQuery().orderByAsc(ShopType::getSort).list();
        if (shopTypeList.isEmpty()){
            throw new RuntimeException("没有商品类型");
        }
        String shopType = JSONUtil.toJsonStr(shopTypeList);
        redisTemplate.opsForValue().set(EnumConstants.SHOP_TYPE.getValue(),shopType);
        return Result.ok(shopTypeList);
    }
}

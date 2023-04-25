package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hmdp.config.RedisConfig;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Collections;
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
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedissonClient redissonClient2;
    @Autowired
    private RedisWorker redisWorker;
    private static final DefaultRedisScript<Long> SECKILL_QUALIFY;
    static {
        SECKILL_QUALIFY = new DefaultRedisScript<>();
        SECKILL_QUALIFY.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_QUALIFY.setResultType(Long.class);
    }
    @Override
    public Result seckillVoucher(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherMapper.selectById(voucherId);
        Long userId = UserHolder.getUser().getId();
        Long result = redisTemplate.execute(
                SECKILL_QUALIFY,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString());
        int r = result.intValue();
        if (r != 0){
            return Result.fail(r == 1 ? "库存不足" : "用户不能重复抢");
        }
        long orderId = redisWorker.nextId("order");

        return Result.ok(orderId);

//            createOrder(voucherId,userId,LocalDateTime.now(),seckillVoucher);


    }
//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        LocalDateTime now = LocalDateTime.now();
//        SeckillVoucher seckillVoucher = seckillVoucherMapper.selectById(voucherId);
//        LocalDateTime beginTime = seckillVoucher.getBeginTime();
//        LocalDateTime endTime = seckillVoucher.getEndTime();
//        if (now.isBefore(beginTime)){
//            throw new RuntimeException("活动尚未开始");
//        }
//        if (now.isAfter(endTime)){
//            throw new RuntimeException("活动已经结束");
//        }
//        long userId = UserHolder.getUser().getId();
//
////        SimpleRedisLock redisLock = new SimpleRedisLock(redisTemplate,"order:"+userId);
//        RLock redisLock = redissonClient.getLock("lock:order:"+userId);
//        RLock redisLock2 = redissonClient2.getLock("lock:order:"+userId);
//        RLock multiLock = redissonClient.getMultiLock(redisLock,redisLock2);
//        try {
//            boolean suc9cess = multiLock.tryLock(1,  TimeUnit.SECONDS);
//            if (!success){
//                return Result.fail("一个用户只能抢一次");
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createOrder(voucherId,userId,now,seckillVoucher);
//        }finally {
//            redisLock.unlock();
//        }
//
//    }
    @Transactional(rollbackFor = Exception.class)
    public Result createOrder(Long voucherId,Long userId,LocalDateTime now,SeckillVoucher seckillVoucher){
        if (seckillVoucher.getStock()<1){
            return Result.fail("库存不足");
        }

        VoucherOrder voucherUser = lambdaQuery().eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId,voucherId).one();
        if (BeanUtil.isNotEmpty(voucherUser)){
            return Result.fail("一个用户只能抢一次");
        }
        seckillVoucherMapper.
                update(seckillVoucher,
                        new LambdaUpdateWrapper<SeckillVoucher>()
                                .setSql("stock = stock -1")
                                .eq(SeckillVoucher::getVoucherId,voucherId)
                                .gt(SeckillVoucher::getStock,0));
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = new RedisWorker(redisTemplate).nextId("order");
        VoucherOrder voucherOrder1 = voucherOrder
                .setVoucherId(voucherId)
                .setUserId(userId)
                .setStatus(1)
                .setPayType(1)
                .setPayTime(now)
                .setId(orderId)
                .setUseTime(now);
        save(voucherOrder1);
        return Result.ok(orderId);
    }
}

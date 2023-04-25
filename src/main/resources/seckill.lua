--1.参数列表
--1.1优惠券id
local voucherId = ARGV[1]
--1.2用户id
local userId = ARGV[2]
--2.数据key
--2.1库存key
local stockKey = 'seckill:stock:' .. voucherId
--2.2订单key
local orderKey = 'seckill:order:' .. voucherId


if (tonumber(redis.call('get',stockKey)) <= 0 ) then
        --库存不足，返回1
    return 1
end
if (redis.call('sismember', orderKey, userId) == 1) then
    --用户存在，不允许重复下单
    return 2
end
redis.call('incrby',stockKey,-1)
redis.call('sadd',orderKey,userId)
return 0

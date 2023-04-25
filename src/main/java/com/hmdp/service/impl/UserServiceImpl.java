package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Editor;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.EnumConstants;
import com.hmdp.utils.JwtUtil;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

        @Autowired
        private StringRedisTemplate redisTemplate;
    @Override
    public void sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)){
            throw new RuntimeException("手机号格式错误");
        }
//            生成一个验证码
       String code =  RandomUtil.randomNumbers(6);
       log.info(code);
//            存入到redis里面
        redisTemplate.opsForValue().set( EnumConstants.CODE_PRE.getValue()+phone,code, EnumConstants.CODE_TIME.getKey(), TimeUnit.SECONDS);


    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)){
            throw new RuntimeException("手机号格式错误");
        }
        String cacheCode = redisTemplate.opsForValue().get(EnumConstants.CODE_PRE.getValue() + phone);
        String code = loginForm.getCode();
        if (!cacheCode.equals(code)||cacheCode==null){
            throw new RuntimeException("验证码错误");
        }
        //    1.根据手机号获取用户信息
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        //    2.如果为空则将用户存入数据库
        String name =  RandomUtil.randomString(6);
        String nickName = EnumConstants.NICK_NAME.getValue()+name;
        if (BeanUtil.isEmpty(user)){
            User userN = new User();
            userN.setPhone(phone);
            userN.setNickName(nickName);
            userN.setIcon("游客");
            save(userN);
            user = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //生成token
        String token = JwtUtil.createToken(loginForm,userDTO);
        redisTemplate.opsForValue().set(EnumConstants.TOKEN.getValue()+phone,token);
        redisTemplate.expire(EnumConstants.TOKEN.getValue()+phone,EnumConstants.TOKEN.getKey(),TimeUnit.MINUTES);
        return Result.ok(token);
    }

}

package com.hmdp.interceptor;

import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.EnumConstants;
import com.hmdp.utils.JwtUtil;
import com.hmdp.utils.NoLogin;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class refreshInterceptor implements HandlerInterceptor {
       private StringRedisTemplate stringRedisTemplate;
       public refreshInterceptor(StringRedisTemplate stringRedisTemplate){
           this.stringRedisTemplate = stringRedisTemplate;
       }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return true;
        }
        String phone = JwtUtil.getPhone(token);
        //刷新token有效期
        stringRedisTemplate.expire(EnumConstants.TOKEN.getValue() + phone,EnumConstants.TOKEN.getKey(), TimeUnit.MINUTES);
       return true;
    }
}

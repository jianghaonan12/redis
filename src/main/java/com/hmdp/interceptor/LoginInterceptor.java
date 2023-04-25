package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.EnumConstants;
import com.hmdp.utils.JwtUtil;
import com.hmdp.utils.NoLogin;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor {
       private StringRedisTemplate stringRedisTemplate;
       public LoginInterceptor(StringRedisTemplate stringRedisTemplate){
           this.stringRedisTemplate = stringRedisTemplate;
       }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        //判断方法，类上面是否有自定义注解，有的话放行
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(NoLogin.class) || method.getDeclaringClass().isAnnotationPresent(NoLogin.class)){
            return true;
        }
        //没有自定义注解，就判断请求头里面的token，为空直接挂掉
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return false;
        }
        //从token里面获取手机号，如果token不真实，则直接报错
        String phone = JwtUtil.getPhone(token);
        //从redis里面获取token
        String redisToken = stringRedisTemplate.opsForValue().get(EnumConstants.TOKEN.getValue() + phone);
        //判断请求头里面的token与redis里面的token是否一样，不一样的话，删除userDto并且挂掉
        if (!token.equals(redisToken)){
            UserHolder.removeUser();
            return false;
        }
        //根据token获取userDto
        UserDTO userDto = JwtUtil.getUserDto(token);
        //将userDto存入ThreadLocal
        UserHolder.saveUser(userDto);
       return true;
    }
}

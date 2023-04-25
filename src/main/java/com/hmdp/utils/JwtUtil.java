package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.UserDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;

public class JwtUtil {
    public static String createToken(LoginFormDTO loginForm, UserDTO userDTO) {
        //JWT
        //定义头
        HashMap<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        //定义负载
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("pho", loginForm.getPhone());
        payload.put("name", "panda");
        payload.put("id",userDTO.getId());
        payload.put("nickNam",userDTO.getNickName());
        payload.put("icon",userDTO.getIcon());
        //生成Token
        String token = Jwts.builder()
                .setHeader(header)
                .setClaims(payload)
                .setIssuedAt(new Date())
                .setId(UUID.randomUUID().toString(true))
                .setSubject("admin")
                .signWith(SignatureAlgorithm.HS256, loginForm.getCode())
                .compact();
        return token;
    }

    public static String getPhone(String token) {
        DecodedJWT decode = null;
        try {
            decode = JWT.decode(token);
        } catch (Exception e) {
            throw new RuntimeException("token错误，请重新输入你的token");
        }
        String phone = decode.getClaim("pho").asString();
        return phone;
    }
    public static UserDTO getUserDto(String token){
        DecodedJWT decode = null;
        try {
            decode = JWT.decode(token);
        } catch (Exception e) {
            throw new RuntimeException("token错误，请重新输入你的token");
        }
        Long id = decode.getClaim("id").asLong();
        String nickName = decode.getClaim("nickName").asString();
        String icon = decode.getClaim("icon").asString();
        UserDTO userDTO = new UserDTO();
       return userDTO.setId(id).setNickName(nickName).setIcon(icon);
    }
}

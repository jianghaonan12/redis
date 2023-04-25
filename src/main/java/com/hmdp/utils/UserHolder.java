package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}

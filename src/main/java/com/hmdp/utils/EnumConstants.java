package com.hmdp.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumConstants {
    NICK_NAME(0,"user_"),
    CODE_PRE(0,"login:code:"),
    TOKEN(30,"token:"),
    SHOP(30,"shop:"),
    SHOP_TYPE(0,"shopType"),
    SHOP_NULL(2,""),
    CODE_TIME(60,"user");


    private Integer key;
    private String value;

}

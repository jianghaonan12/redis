package com.hmdp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}

package com.cupk.dto;

import lombok.Data;

/**
 * 找回密码请求
 */
@Data
public class ResetPasswordDTO {
    private String email;
    private String code;
    private String password;
}

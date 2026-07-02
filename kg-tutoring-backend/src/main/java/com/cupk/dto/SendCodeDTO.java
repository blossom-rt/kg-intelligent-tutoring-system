package com.cupk.dto;

import lombok.Data;

/**
 * 发送验证码请求
 */
@Data
public class SendCodeDTO {
    private String email;
    private String type;
}

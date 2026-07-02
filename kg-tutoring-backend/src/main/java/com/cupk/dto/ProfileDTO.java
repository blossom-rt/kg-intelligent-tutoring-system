package com.cupk.dto;

import lombok.Data;

/**
 * 修改个人信息请求
 */
@Data
public class ProfileDTO {
    private String realName;
    private String email;
}

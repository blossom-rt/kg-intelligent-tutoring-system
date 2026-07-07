package com.cupk.dto;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class UpdatePasswordDTO {
    private String oldPassword;
    private String newPassword;
}

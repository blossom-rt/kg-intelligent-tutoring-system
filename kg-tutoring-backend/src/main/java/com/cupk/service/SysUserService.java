package com.cupk.service;

import com.cupk.dto.LoginDTO;
import com.cupk.dto.LoginVO;

/**
 * 用户服务
 */
public interface SysUserService {

    /**
     * 用户登录
     *
     * @param dto 登录请求（用户名 + 密码）
     * @return 登录响应（token + 角色），若账号或密码错误返回 null
     */
    LoginVO login(LoginDTO dto);
}

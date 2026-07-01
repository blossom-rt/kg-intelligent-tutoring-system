package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.dto.LoginDTO;
import com.cupk.dto.LoginVO;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.SysUser;
import com.cupk.service.SysUserService;
import com.cupk.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 根据用户名查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );

        // 2. 用户不存在或密码错误
        if (user == null || !user.getPassword().equals(dto.getPassword())) {
            return null;
        }

        // 3. 生成 token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginVO(token, user.getRole());
    }
}

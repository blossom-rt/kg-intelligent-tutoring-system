package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.dto.LoginDTO;
import com.cupk.dto.LoginVO;
import com.cupk.mapper.SysRoleMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.SysRole;
import com.cupk.pojo.SysUser;
import com.cupk.service.SysUserService;
import com.cupk.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 根据用户名查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );

        // 2. 用户不存在或密码错误
        if (user == null || !user.getPassword()
                .equals(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            return null;
        }

        // 3. 检查账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            return null;
        }

        // 4. 查询角色
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        if (role == null) {
            return null;
        }

        // 5. 校验前端选择的角色是否匹配
        if (dto.getRole() != null && !role.getRoleCode().equals(dto.getRole())) {
            return null;
        }

        // 6. 生成 token（使用 role_code）
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), role.getRoleCode());

        return new LoginVO(token, role.getRoleCode());
    }
}

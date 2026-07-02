package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.dto.LoginDTO;
import com.cupk.dto.LoginVO;
import com.cupk.dto.RegisterDTO;
import com.cupk.dto.ResetPasswordDTO;
import com.cupk.mapper.SysEmailCodeMapper;
import com.cupk.mapper.SysRoleMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.SysEmailCode;
import com.cupk.pojo.SysRole;
import com.cupk.pojo.SysUser;
import com.cupk.service.SysUserService;
import com.cupk.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysEmailCodeMapper emailCodeMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );

        if (user == null || !user.getPassword()
                .equals(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            return null;
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return null;
        }

        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        if (role == null) return null;

        if (dto.getRole() != null && !role.getRoleCode().equals(dto.getRole())) {
            return null;
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), role.getRoleCode());
        return new LoginVO(token, role.getRoleCode());
    }

    @Override
    public void register(RegisterDTO dto) {
        if (sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new RuntimeException("账号已存在");
        }
        if (sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail())) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        SysEmailCode latestCode = emailCodeMapper.selectOne(
                new LambdaQueryWrapper<SysEmailCode>()
                        .eq(SysEmailCode::getEmail, dto.getEmail())
                        .eq(SysEmailCode::getType, "register")
                        .eq(SysEmailCode::getIsUsed, 0)
                        .orderByDesc(SysEmailCode::getCreateTime)
                        .last("LIMIT 1")
        );

        if (latestCode == null || !latestCode.getCode().equals(dto.getCode())) {
            throw new RuntimeException("验证码错误");
        }
        if (latestCode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("验证码已过期");
        }

        latestCode.setIsUsed(1);
        emailCodeMapper.updateById(latestCode);

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setRoleId(3);
        user.setStatus(1);
        sysUserMapper.insert(user);
    }

    @Override
    public void sendEmailCode(String email, String type) {
        LocalDateTime oneMinAgo = LocalDateTime.now().minusSeconds(60);
        if (emailCodeMapper.selectCount(
                new LambdaQueryWrapper<SysEmailCode>()
                        .eq(SysEmailCode::getEmail, email)
                        .eq(SysEmailCode::getType, type)
                        .gt(SysEmailCode::getCreateTime, oneMinAgo)) > 0) {
            throw new RuntimeException("60秒内已发送过验证码");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));

        SysEmailCode emailCode = new SysEmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setType(type);
        emailCode.setExpireTime(LocalDateTime.now().plusMinutes(5));
        emailCode.setIsUsed(0);
        emailCodeMapper.insert(emailCode);

        System.out.println("======= 验证码：[" + code + "] 发送至邮箱：" + email + " =======");
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail()));
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }

        SysEmailCode latestCode = emailCodeMapper.selectOne(
                new LambdaQueryWrapper<SysEmailCode>()
                        .eq(SysEmailCode::getEmail, dto.getEmail())
                        .eq(SysEmailCode::getType, "reset")
                        .eq(SysEmailCode::getIsUsed, 0)
                        .orderByDesc(SysEmailCode::getCreateTime)
                        .last("LIMIT 1")
        );

        if (latestCode == null || !latestCode.getCode().equals(dto.getCode())) {
            throw new RuntimeException("验证码错误");
        }
        if (latestCode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("验证码已过期");
        }

        latestCode.setIsUsed(1);
        emailCodeMapper.updateById(latestCode);

        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        sysUserMapper.updateById(user);
    }
}

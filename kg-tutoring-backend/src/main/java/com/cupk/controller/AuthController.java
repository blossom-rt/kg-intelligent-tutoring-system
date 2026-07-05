package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.Result;
import com.cupk.dto.*;
import com.cupk.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 —— 登录、注册、验证码、找回密码
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    /** 账号密码登录 */
    @OperLog(module = "认证管理", operation = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO vo = sysUserService.login(dto);
        if (vo == null) {
            return Result.error(401, "账号或密码错误");
        }
        return Result.success(vo);
    }

    /** 获取当前用户信息 */
    @GetMapping("/info")
    public Result<LoginVO> info() {
        return Result.success(sysUserService.getCurrentUserInfo());
    }

    /** 学生自主注册 */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        sysUserService.register(dto);
        return Result.success("注册成功");
    }

    /** 发送邮箱验证码 */
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody SendCodeDTO dto) {
        sysUserService.sendEmailCode(dto.getEmail(), dto.getType());
        return Result.success("验证码已发送");
    }

    /** 邮箱找回密码 */
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        sysUserService.resetPassword(dto);
        return Result.success("密码重置成功");
    }
}

package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.dto.LoginVO;
import com.cupk.dto.ProfileDTO;
import com.cupk.dto.UpdatePasswordDTO;
import com.cupk.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 —— 个人信息管理
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    /** 修改登录密码 */
    @PutMapping("/update-password")
    public Result<?> updatePassword(@RequestBody UpdatePasswordDTO dto) {
        sysUserService.updatePassword(UserContext.getUserId(), dto);
        return Result.success("密码修改成功");
    }

    /** 获取个人信息 */
    @GetMapping("/profile")
    public Result<LoginVO> getProfile() {
        return Result.success(sysUserService.getCurrentUserInfo());
    }

    /** 修改个人信息 */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody ProfileDTO dto) {
        sysUserService.updateProfile(UserContext.getUserId(), dto);
        return Result.success("信息修改成功");
    }
}

package com.cupk.controller;

import com.cupk.dto.LoginDTO;
import com.cupk.dto.LoginVO;
import com.cupk.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    /**
     * 用户登录
     * 前端路径：POST /api/kg/user/login  →  Vite 代理  →  POST /user/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        LoginVO vo = sysUserService.login(dto);

        if (vo == null) {
            return ResponseEntity.status(401).body(Map.of("error", "账号或密码错误"));
        }

        return ResponseEntity.ok(vo);
    }
}

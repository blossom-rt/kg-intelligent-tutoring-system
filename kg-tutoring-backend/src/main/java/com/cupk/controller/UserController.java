package com.cupk.controller;

import com.cupk.dto.LoginDTO;
import com.cupk.dto.LoginVO;
import com.cupk.dto.RegisterDTO;
import com.cupk.dto.ResetPasswordDTO;
import com.cupk.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        LoginVO vo = sysUserService.login(dto);
        if (vo == null) {
            return ResponseEntity.status(401).body(Map.of("error", "账号或密码错误"));
        }
        return ResponseEntity.ok(vo);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        try {
            sysUserService.register(dto);
            return ResponseEntity.ok(Map.of("message", "注册成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestParam String email, @RequestParam String type) {
        try {
            sysUserService.sendEmailCode(email, type);
            return ResponseEntity.ok(Map.of("message", "验证码已发送"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        try {
            sysUserService.resetPassword(dto);
            return ResponseEntity.ok(Map.of("message", "密码已重置"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

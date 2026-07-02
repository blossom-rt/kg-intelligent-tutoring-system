package com.cupk.service;

import com.cupk.dto.LoginDTO;
import com.cupk.dto.LoginVO;
import com.cupk.dto.RegisterDTO;
import com.cupk.dto.ResetPasswordDTO;

public interface SysUserService {
    LoginVO login(LoginDTO dto);
    void register(RegisterDTO dto);
    void sendEmailCode(String email, String type);
    void resetPassword(ResetPasswordDTO dto);
}

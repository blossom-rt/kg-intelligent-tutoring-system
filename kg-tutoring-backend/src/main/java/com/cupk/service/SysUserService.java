package com.cupk.service;

import com.cupk.dto.*;
import com.cupk.pojo.SysRole;
import com.cupk.pojo.SysUser;

import java.util.List;

/**
 * 用户服务
 */
public interface SysUserService {

    /** 用户登录 */
    LoginVO login(LoginDTO dto);

    /** 获取当前用户信息 */
    LoginVO getCurrentUserInfo();

    /** 学生注册 */
    void register(RegisterDTO dto);

    /** 发送邮箱验证码 */
    void sendEmailCode(String email, String type);

    /** 找回密码 */
    void resetPassword(ResetPasswordDTO dto);

    /** 修改密码 */
    void updatePassword(Integer userId, UpdatePasswordDTO dto);

    /** 根据 ID 查用户 */
    SysUser getUserById(Integer id);

    /** 修改个人信息 */
    void updateProfile(Integer userId, ProfileDTO dto);

    // 管理员方法

    /** 查询角色列表 */
    List<SysRole> listRoles();

    /** 查询用户列表 */
    List<SysUser> listUsers(String keyword, Integer roleId);

    /** 管理员创建用户 */
    void createUser(SysUser user);

    /** 管理员更新用户 */
    void updateUser(SysUser user);

    /** 管理员删除用户 */
    void deleteUser(Integer id);

    /** 启用 / 禁用账号 */
    void updateUserStatus(Integer id, Integer status);
}

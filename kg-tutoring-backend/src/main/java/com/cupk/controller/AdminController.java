package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.SysOperLog;
import com.cupk.pojo.SysRole;
import com.cupk.pojo.SysUser;
import com.cupk.service.LogService;
import com.cupk.service.SysRoleService;
import com.cupk.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理员控制器 —— 角色管理、用户管理、日志查询（管理员操作）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SysRoleService sysRoleService;
    private final SysUserService sysUserService;
    private final LogService logService;
    private final SysUserMapper sysUserMapper;

    /**
     * 检查当前用户是否为管理员，否则抛出无权限异常
     */
    private void checkAdmin() {
        if (!"admin".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    // 角色管理

    /**
     * 获取所有角色列表
     */
    @GetMapping("/roles")
    public Result<?> listRoles() {
        checkAdmin();
        return Result.success(sysRoleService.listAll());
    }

    /**
     * 新增角色
     */
    @OperLog(module = "角色管理", operation = "新增角色")
    @PostMapping("/roles")
    public Result<?> createRole(@RequestBody SysRole role) {
        checkAdmin();
        sysRoleService.save(role);
        return Result.success("新增角色成功");
    }

    /**
     * 修改角色
     */
    @OperLog(module = "角色管理", operation = "修改角色")
    @PutMapping("/roles/{id}")
    public Result<?> updateRole(@PathVariable Integer id, @RequestBody SysRole role) {
        checkAdmin();
        role.setId(id);
        sysRoleService.update(role);
        return Result.success("修改角色成功");
    }

    /**
     * 删除角色
     */
    @OperLog(module = "角色管理", operation = "删除角色")
    @DeleteMapping("/roles/{id}")
    public Result<?> deleteRole(@PathVariable Integer id) {
        checkAdmin();
        sysRoleService.delete(id);
        return Result.success("删除角色成功");
    }

    // 用户管理

    /**
     * 查询用户列表
     */
    @GetMapping("/users")
    public Result<List<SysUser>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roleId) {
        checkAdmin();
        return Result.success(sysUserService.listUsers(keyword, roleId));
    }

    /**
     * 新增用户
     */
    @OperLog(module = "用户管理", operation = "新增用户")
    @PostMapping("/users")
    public Result<?> createUser(@RequestBody SysUser user) {
        checkAdmin();
        sysUserService.createUser(user);
        return Result.success("新增用户成功");
    }

    /**
     * 修改用户信息
     */
    @OperLog(module = "用户管理", operation = "修改用户")
    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Integer id, @RequestBody SysUser user) {
        checkAdmin();
        user.setId(id);
        sysUserService.updateUser(user);
        return Result.success("修改用户成功");
    }

    /**
     * 删除用户
     */
    @OperLog(module = "用户管理", operation = "删除用户")
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Integer id) {
        checkAdmin();
        sysUserService.deleteUser(id);
        return Result.success("删除用户成功");
    }

    /**
     * 启用/禁用用户
     *
     * 请求体：{ "status": 1 }  1-启用 0-禁用
     */
    @OperLog(module = "用户管理", operation = "启用/禁用用户")
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        checkAdmin();
        Integer status = (Integer) body.get("status");
        sysUserService.updateUserStatus(id, status);
        return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
    }

    // 日志管理

    /**
     * 查询 AI 调用日志
     */
    @GetMapping("/ai-logs")
    public Result<?> listAiLogs() {
        checkAdmin();
        List<com.cupk.pojo.AiCallLog> logs = logService.listAiLog(null);
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (com.cupk.pojo.AiCallLog log : logs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("userId", log.getUserId());
            if (log.getUserId() != null) {
                SysUser u = sysUserMapper.selectById(log.getUserId());
                item.put("userName", u != null ? u.getRealName() : ("用户" + log.getUserId()));
            } else {
                item.put("userName", "-");
            }
            item.put("scene", log.getScene());
            item.put("prompt", log.getPrompt());
            item.put("result", log.getResult());
            item.put("callDuration", log.getCallDuration());
            item.put("status", log.getStatus());
            item.put("createTime", log.getCreateTime());
            enriched.add(item);
        }
        return Result.success(enriched);
    }

    /**
     * 查询操作日志，可按模块筛选
     *
     * @param module 操作模块（可选）
     */
    @GetMapping("/oper-logs")
    public Result<?> listOperLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        checkAdmin();
        java.time.LocalDateTime start = startDate != null ? java.time.LocalDateTime.parse(startDate + "T00:00:00") : null;
        java.time.LocalDateTime end = endDate != null ? java.time.LocalDateTime.parse(endDate + "T23:59:59") : null;
        List<SysOperLog> logs = logService.listOperLog(module, start, end);
        // 补充操作人姓名
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (SysOperLog log : logs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("userId", log.getUserId());
            if (log.getUserId() != null) {
                SysUser u = sysUserMapper.selectById(log.getUserId());
                item.put("userName", u != null ? u.getRealName() : ("用户" + log.getUserId()));
            } else {
                item.put("userName", "-");
            }
            item.put("module", log.getModule());
            item.put("operation", log.getOperation());
            item.put("ip", log.getIp());
            item.put("createTime", log.getCreateTime());
            enriched.add(item);
        }
        return Result.success(enriched);
    }
}

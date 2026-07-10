package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.SysOperLog;
import com.cupk.pojo.SysRole;
import com.cupk.pojo.SysUser;

import org.springframework.http.*;
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

    /**
     * 导出用户（CSV）
     */
    @GetMapping("/users/export")
    public ResponseEntity<byte[]> exportUsers() {
        checkAdmin();
        List<SysUser> users = sysUserService.listUsers(null, null);
        StringBuilder csv = new StringBuilder("用户名,姓名,邮箱,角色,状态\n");
        for (SysUser u : users) {
            String roleName = u.getRole() != null ? u.getRole().getRoleName() : "";
            String status = u.getStatus() != null && u.getStatus() == 1 ? "启用" : "禁用";
            csv.append(String.format("%s,%s,%s,%s,%s\n",
                    u.getUsername() != null ? u.getUsername() : "",
                    u.getRealName() != null ? u.getRealName() : "",
                    u.getEmail() != null ? u.getEmail() : "",
                    roleName, status));
        }
        String csvStr = csv.toString();
        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] csvBytes = csvStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bytes = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, bytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, bytes, bom.length, csvBytes.length);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename("users.csv").build());
        return new ResponseEntity<>(bytes, headers, org.springframework.http.HttpStatus.OK);
    }

    /**
     * 导入用户（CSV）
     */
    @OperLog(module = "用户管理", operation = "批量导入用户")
    @PostMapping("/users/import")
    public Result<?> importUsers(@RequestBody Map<String, Object> body) {
        checkAdmin();
        String csvData = (String) body.get("csvData");
        if (csvData == null || csvData.isBlank()) return Result.error("数据为空");
        String[] lines = csvData.split("\n");
        int success = 0, fail = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank() || line.startsWith("用户名")) continue;
            String[] parts = line.split(",");
            if (parts.length < 3) { fail++; continue; }
            try {
                SysUser user = new SysUser();
                user.setUsername(parts[0].trim());
                user.setRealName(parts.length > 1 ? parts[1].trim() : parts[0].trim());
                user.setEmail(parts.length > 2 && !parts[2].isBlank() ? parts[2].trim() : null);
                user.setPassword("e10adc3949ba59abbe56e057f20f883e"); // 默认 123456
                // 角色：通过角色名查找
                if (parts.length > 3 && !parts[3].isBlank()) {
                    List<com.cupk.pojo.SysRole> roles = sysUserService.listRoles();
                    for (com.cupk.pojo.SysRole r : roles) {
                        if (r.getRoleName().equals(parts[3].trim())) {
                            user.setRoleId(r.getId()); break;
                        }
                    }
                }
                if (user.getRoleId() == null) user.setRoleId(3);
                user.setStatus(1);
                sysUserService.createUser(user);
                success++;
            } catch (Exception e) { fail++; }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return Result.success(result);
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

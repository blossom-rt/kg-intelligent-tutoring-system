package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.CrossSubjectTheme;
import com.cupk.service.CrossSubjectThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跨学科主题控制器 —— 跨学科学习主题的增删改查与发布管理（教师操作）
 */
@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final CrossSubjectThemeService crossSubjectThemeService;

    /**
     * 检查当前用户是否为教师，否则抛出无权限异常
     */
    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询所有主题
     */
    @GetMapping
    public Result<List<CrossSubjectTheme>> list(
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status) {
        return Result.success(crossSubjectThemeService.list(difficulty, status));
    }

    /**
     * 获取主题详情
     */
    @GetMapping("/{id}")
    public Result<CrossSubjectTheme> detail(@PathVariable Integer id) {
        return Result.success(crossSubjectThemeService.getById(id));
    }

    /**
     * 新增主题（仅教师）
     */
    @OperLog(module = "主题管理", operation = "新增主题")
    @PostMapping
    public Result<?> create(@RequestBody CrossSubjectTheme theme) {
        checkTeacher();
        crossSubjectThemeService.save(theme);
        return Result.success("新增主题成功");
    }

    /**
     * 修改主题（仅教师）
     */
    @OperLog(module = "主题管理", operation = "修改主题")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody CrossSubjectTheme theme) {
        checkTeacher();
        theme.setId(id);
        crossSubjectThemeService.update(theme);
        return Result.success("修改主题成功");
    }

    /**
     * 删除主题（仅教师）
     */
    @OperLog(module = "主题管理", operation = "删除主题")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        crossSubjectThemeService.delete(id);
        return Result.success("删除主题成功");
    }

    /**
     * 切换主题的发布/取消发布状态（仅教师）
     */
    @OperLog(module = "主题管理", operation = "发布/下架主题")
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Integer id) {
        checkTeacher();
        crossSubjectThemeService.toggleStatus(id);
        return Result.success("主题状态已更新");
    }
}

package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.CrossThemeNodeMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.CrossSubjectTheme;
import com.cupk.pojo.CrossThemeNode;
import com.cupk.pojo.Course;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.SysUser;
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
    private final SysUserMapper sysUserMapper;
    private final CrossThemeNodeMapper crossThemeNodeMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final com.cupk.mapper.CourseMapper courseMapper;

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
        // 教师角色时自动只显示自己发布的主题
        if ("teacher".equals(UserContext.getRole())) {
            List<CrossSubjectTheme> all = crossSubjectThemeService.list(difficulty, status);
            all.removeIf(t -> t.getPublisherId() == null || !t.getPublisherId().equals(UserContext.getUserId()));
            // 填充教师姓名
            for (CrossSubjectTheme t : all) {
                if (t.getPublisherId() != null) {
                    SysUser user = sysUserMapper.selectById(t.getPublisherId());
                    if (user != null) t.setPublisherName(user.getRealName());
                }
            }
            return Result.success(all);
        }
        List<CrossSubjectTheme> list = crossSubjectThemeService.list(difficulty, status);
        // 填充发布教师姓名
        for (CrossSubjectTheme t : list) {
            if (t.getPublisherId() != null) {
                SysUser user = sysUserMapper.selectById(t.getPublisherId());
                if (user != null) t.setPublisherName(user.getRealName());
            }
        }
        return Result.success(list);
    }

    /**
     * 获取主题详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Integer id) {
        CrossSubjectTheme theme = crossSubjectThemeService.getById(id);
        if (theme == null) return Result.error("主题不存在");
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", theme.getId());
        result.put("themeName", theme.getThemeName());
        result.put("description", theme.getDescription());
        result.put("difficulty", theme.getDifficulty());
        result.put("status", theme.getStatus());
        result.put("createTime", theme.getCreateTime());
        // 计算预计学习时长和涉及学科
        List<CrossThemeNode> mappings = crossThemeNodeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrossThemeNode>()
                        .eq(CrossThemeNode::getThemeId, id));
        if (!mappings.isEmpty()) {
            List<Integer> nodeIds = mappings.stream().map(CrossThemeNode::getNodeId).toList();
            List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeNode>()
                            .in(KnowledgeNode::getId, nodeIds));
            int totalMinutes = nodes.stream()
                    .mapToInt(n -> n.getExpectedMinutes() != null ? n.getExpectedMinutes() : 0)
                    .sum();
            result.put("estimatedTime", totalMinutes);
            // 涉及学科
            List<Integer> courseIds = nodes.stream()
                    .map(KnowledgeNode::getCourseId).filter(java.util.Objects::nonNull).distinct().toList();
            if (!courseIds.isEmpty()) {
                List<Course> courses = courseMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                                .in(Course::getId, courseIds));
                List<String> subjectNames = courses.stream()
                        .map(c -> c.getSubject() != null ? c.getSubject() : c.getCourseName())
                        .distinct().toList();
                result.put("subjects", subjectNames);
            } else {
                result.put("subjects", java.util.Collections.emptyList());
            }
        } else {
            result.put("estimatedTime", 0);
            result.put("subjects", java.util.Collections.emptyList());
        }
        return Result.success(result);
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

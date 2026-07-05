package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.Course;
import com.cupk.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器 —— 课程的增删改查（管理员操作）
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 检查当前用户是否为管理员，否则抛出无权限异常
     */
    private void checkAdmin() {
        if (!"admin".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询课程列表
     */
    @GetMapping
    public Result<List<Course>> list(@RequestParam(required = false) String subject) {
        return Result.success(courseService.list(subject));
    }

    /**
     * 根据 ID 获取课程详情
     */
    @GetMapping("/{id}")
    public Result<Course> getOne(@PathVariable Integer id) {
        return Result.success(courseService.getById(id));
    }

    /**
     * 新增课程（仅管理员）
     */
    @OperLog(module = "课程管理", operation = "新增课程")
    @PostMapping
    public Result<?> create(@RequestBody Course course) {
        checkAdmin();
        courseService.save(course);
        return Result.success("新增课程成功");
    }

    /**
     * 修改课程（仅管理员）
     */
    @OperLog(module = "课程管理", operation = "修改课程")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Course course) {
        checkAdmin();
        course.setId(id);
        courseService.update(course);
        return Result.success("修改课程成功");
    }

    /**
     * 删除课程（仅管理员）
     */
    @OperLog(module = "课程管理", operation = "删除课程")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkAdmin();
        courseService.delete(id);
        return Result.success("删除课程成功");
    }
}

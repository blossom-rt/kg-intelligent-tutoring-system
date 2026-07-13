package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.Course;
import com.cupk.pojo.SysUser;
import com.cupk.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课程控制器 —— 课程的增删改查（管理员操作）
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final SysUserMapper sysUserMapper;

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
    public Result<List<Course>> list(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer teacherId) {
        // 教师角色自动按当前教师过滤
        Integer autoTeacherId = teacherId;
        if ("teacher".equals(UserContext.getRole())) {
            autoTeacherId = UserContext.getUserId();
        }
        List<Course> courses = courseService.list(subject, autoTeacherId);
        // 填充教师姓名
        for (Course c : courses) {
            if (c.getTeacherId() != null) {
                SysUser teacher = sysUserMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
        }
        return Result.success(courses);
    }

    /**
     * 推荐与所选课程相关度高的其他课程
     */
    @GetMapping("/{id}/recommendations/related")
    public Result<List<Map<String, Object>>> relatedRecommendations(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(courseService.recommendRelatedCourses(id, limit));
    }

    /**
     * 学习这门课程的同学还学习了哪些课程
     */
    @GetMapping("/{id}/recommendations/also-learned")
    public Result<List<Map<String, Object>>> alsoLearnedRecommendations(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(courseService.recommendAlsoLearnedCourses(id, limit));
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

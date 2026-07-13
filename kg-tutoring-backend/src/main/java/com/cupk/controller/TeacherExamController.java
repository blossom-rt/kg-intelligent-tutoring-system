package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.CourseMapper;
import com.cupk.pojo.Course;
import com.cupk.service.ExamService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 教师考试管理控制器
 */
@RestController
@RequestMapping("/api/teacher/exams")
@RequiredArgsConstructor
public class TeacherExamController {

    private final ExamService examService;
    private final CourseMapper courseMapper;

    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /** 按课程查询测评列表 */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) Integer courseId) {
        checkTeacher();
        if (courseId == null) {
            List<Course> myCourses = courseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                            .eq(Course::getTeacherId, UserContext.getUserId()));
            List<Integer> ids = myCourses.stream().map(Course::getId).collect(Collectors.toList());
            if (ids.isEmpty()) return Result.success(List.of());
            List<Map<String, Object>> all = new java.util.ArrayList<>();
            for (Integer id : ids) {
                all.addAll(examService.listForTeacher(id));
            }
            return Result.success(all);
        }
        return Result.success(examService.listForTeacher(courseId));
    }

    /** 创建测评 */
    @OperLog(module = "测评管理", operation = "创建测评")
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        checkTeacher();
        Integer courseId = (Integer) body.get("courseId");
        String examName = (String) body.get("examName");
        @SuppressWarnings("unchecked")
        List<Integer> questionIds = (List<Integer>) body.get("questionIds");
        Integer totalScore = (Integer) body.get("totalScore");
        examService.createExam(examName, courseId, questionIds, totalScore);
        return Result.success("创建测评成功");
    }

    /** 修改测评 */
    @OperLog(module = "测评管理", operation = "修改测评")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        checkTeacher();
        examService.updateExam(id, body);
        return Result.success("修改测评成功");
    }

    /** 删除测评 */
    @OperLog(module = "测评管理", operation = "删除测评")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        examService.deleteExam(id);
        return Result.success("删除测评成功");
    }
}

package com.cupk.controller;

import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.service.ExamRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师考试管理控制器
 */
@RestController
@RequestMapping("/api/teacher/exams")
@RequiredArgsConstructor
public class TeacherExamController {

    private final ExamRecordService examRecordService;

    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /** 按课程查询测评列表 */
    @GetMapping
    public Result<?> list(@RequestParam Integer courseId) {
        checkTeacher();
        return Result.success(examRecordService.listByCourse(courseId));
    }

    /** 创建测评 */
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        checkTeacher();
        Integer courseId = (Integer) body.get("courseId");
        List<Integer> questionIds = (List<Integer>) body.get("questionIds");
        Integer totalScore = (Integer) body.get("totalScore");
        examRecordService.createExam(courseId, questionIds, totalScore);
        return Result.success("创建测评成功");
    }

    /** 修改测评 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        checkTeacher();
        examRecordService.updateExam(id, body);
        return Result.success("修改测评成功");
    }

    /** 删除测评 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        examRecordService.deleteExam(id);
        return Result.success("删除测评成功");
    }
}

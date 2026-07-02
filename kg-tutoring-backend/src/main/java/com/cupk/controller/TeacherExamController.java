package com.cupk.controller;

import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.service.ExamRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * 教师考试管理控制器 —— 考试的创建与管理（教师操作）
 */
@RestController
@RequestMapping("/api/teacher/exams")
@RequiredArgsConstructor
public class TeacherExamController {

    private final ExamRecordService examRecordService;

    /**
     * 检查当前用户是否为教师，否则抛出无权限异常
     */
    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 根据课程查询考试列表
     *
     * @param courseId 课程 ID
     */
    @GetMapping
    public Result<?> list(@RequestParam Integer courseId) {
        checkTeacher();
        // TODO: ExamRecordService 暂不支持按课程查询，需扩展接口添加 listByCourse 方法
        return Result.success(Collections.emptyList());
    }

    /**
     * 创建考试
     *
     * 请求体示例：{ "courseId": 1, "questionIds": [1,2,3], "totalScore": 100 }
     */
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        checkTeacher();
        // TODO: ExamRecordService 暂不支持创建考试，需扩展接口添加 save 方法
        return Result.success("功能待实现");
    }

    /**
     * 修改考试
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        checkTeacher();
        // TODO: ExamRecordService 暂不支持修改考试，需扩展接口添加 update 方法
        return Result.success("功能待实现");
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        // TODO: ExamRecordService 暂不支持删除考试，需扩展接口添加 delete 方法
        return Result.success("功能待实现");
    }
}

package com.cupk.controller;

import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 教师分析控制器 —— 班级/课程维度的学情分析（教师操作）
 */
@RestController
@RequestMapping("/api/teacher/analysis")
@RequiredArgsConstructor
public class TeacherAnalysisController {

    private final AnalysisService analysisService;

    /**
     * 检查当前用户是否为教师，否则抛出无权限异常
     */
    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 班级整体学情分析
     *
     * @param courseId 课程 ID
     * @return 包含班级整体掌握情况、各知识点平均掌握度、成绩分布等数据
     *
     * TODO: 后续可加入更多分析维度，如学习趋势、薄弱知识点排行等
     */
    @GetMapping("/class")
    public Result<?> classAnalysis(@RequestParam Integer courseId) {
        checkTeacher();
        return Result.success(analysisService.classAnalysis(courseId));
    }
}

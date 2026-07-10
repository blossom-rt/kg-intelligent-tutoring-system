package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生分析控制器 —— 个人学习数据统计与薄弱知识点分析
 */
@RestController
@RequestMapping("/api/student/analysis")
@RequiredArgsConstructor
public class StudentAnalysisController {

    private final AnalysisService analysisService;

    /**
     * 获取当前学生的薄弱知识点列表
     *
     * @return 按掌握程度升序排列的薄弱知识点列表
     */
    @GetMapping("/weak")
    public Result<?> weakKnowledgePoints() {
        Integer userId = UserContext.getUserId();
        return Result.success(analysisService.weakAnalysis(userId));
    }
}

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
     * 获取当前学生的个人学习统计数据
     *
     * @return 包含总学习时长、完成知识点数、正确率等汇总数据
     */
    @GetMapping("/personal")
    public Result<?> personalStats() {
        Integer userId = UserContext.getUserId();
        return Result.success(analysisService.personalAnalysis(userId));
    }

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

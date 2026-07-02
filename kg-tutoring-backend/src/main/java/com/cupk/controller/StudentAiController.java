package com.cupk.controller;

import com.cupk.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生 AI 助手控制器 —— AI 相关功能的占位接口（开发中）
 */
@RestController
@RequestMapping("/api/student/ai")
public class StudentAiController {

    /**
     * 知识点总结（AI 生成）
     *
     * 请求体：{ "nodeId": 1 }
     */
    @PostMapping("/node-summary")
    public Result<?> nodeSummary(@RequestBody Map<String, Object> body) {
        return Result.success("AI 功能开发中");
    }

    /**
     * 错题解析（AI 生成）
     *
     * 请求体：{ "questionId": 1 }
     */
    @PostMapping("/wrong-explain")
    public Result<?> wrongExplain(@RequestBody Map<String, Object> body) {
        return Result.success("AI 功能开发中");
    }

    /**
     * 考试报告（AI 生成）
     *
     * 请求体：{ "examId": 1 }
     */
    @PostMapping("/exam-report")
    public Result<?> examReport(@RequestBody Map<String, Object> body) {
        return Result.success("AI 功能开发中");
    }
}

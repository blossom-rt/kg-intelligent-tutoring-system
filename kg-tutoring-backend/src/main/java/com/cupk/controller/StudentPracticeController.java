package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.Question;
import com.cupk.service.QuestionService;
import com.cupk.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 学生练习控制器 —— 获取练习题、提交答案并自动评分
 */
@RestController
@RequestMapping("/api/student/practice")
@RequiredArgsConstructor
public class StudentPracticeController {

    private final QuestionService questionService;
    private final StudyRecordService studyRecordService;

    /**
     * 根据知识点节点获取练习题目（随机抽取）
     *
     * @param nodeId 知识点节点 ID
     */
    @GetMapping("/questions")
    public Result<?> getQuestions(@RequestParam Integer nodeId) {
        // TODO: 后续可根据难度、历史做错记录等智能选题，当前返回该节点下所有题目
        return Result.success(questionService.listByNode(nodeId));
    }

    /**
     * 提交练习答案并计算得分
     *
     * 请求体示例：
     * {
     *   "nodeId": 1,
     *   "answers": [
     *     { "questionId": 1, "answer": "A" },
     *     { "questionId": 2, "answer": "B" }
     *   ]
     * }
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Map<String, Object> body) {
        Integer userId = UserContext.getUserId();
        Integer nodeId = (Integer) body.get("nodeId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");

        int total = answers.size();
        int correctCount = 0;

        for (Map<String, Object> ans : answers) {
            Integer questionId = (Integer) ans.get("questionId");
            String userAnswer = (String) ans.get("answer");

            // 获取正确答案并比对
            Question question = questionService.getById(questionId);
            String correctAnswer = question != null ? question.getAnswer() : null;
            boolean isCorrect = correctAnswer != null && correctAnswer.equals(userAnswer);
            if (isCorrect) {
                correctCount++;
            }
            // TODO: 记录错题 — WrongQuestionService 暂无 add/save 方法，待后续补充
        }

        // 计算正确率并更新学习记录
        double correctRate = total > 0 ? (double) correctCount / total * 100 : 0;
        int masteryLevel = correctRate >= 80 ? 3 : (correctRate >= 60 ? 2 : 1);
        studyRecordService.updateRecord(userId, nodeId, masteryLevel, BigDecimal.valueOf(correctRate), null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("correctCount", correctCount);
        result.put("correctRate", Math.round(correctRate * 100.0) / 100.0);
        result.put("masteryLevel", masteryLevel);
        result.put("message", correctRate >= 80 ? "表现优秀！" : (correctRate >= 60 ? "继续加油！" : "建议重新学习该知识点"));

        return Result.success(result);
    }
}

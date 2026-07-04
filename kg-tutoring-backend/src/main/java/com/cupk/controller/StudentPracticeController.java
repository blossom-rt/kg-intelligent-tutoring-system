package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.Question;
import com.cupk.pojo.WrongQuestion;
import com.cupk.service.QuestionService;
import com.cupk.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 学生练习控制器
 */
@RestController
@RequestMapping("/api/student/practice")
@RequiredArgsConstructor
public class StudentPracticeController {

    private final QuestionService questionService;
    private final StudyRecordService studyRecordService;
    private final WrongQuestionMapper wrongQuestionMapper;

    /** 获取练习题目 */
    @GetMapping("/questions")
    public Result<?> getQuestions(
            @RequestParam(required = false) Integer nodeId,
            @RequestParam(required = false) Integer questionId) {
        if (questionId != null) {
            // 指定题目重做
            Question q = questionService.getById(questionId);
            return Result.success(q != null ? List.of(q) : List.of());
        }
        if (nodeId == null) {
            return Result.error("请指定知识点或题目");
        }
        return Result.success(questionService.listByNode(nodeId));
    }

    /** 提交答案并评分 */
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

            Question question = questionService.getById(questionId);
            String correctAnswer = question != null ? question.getAnswer() : null;
            boolean isCorrect = correctAnswer != null && correctAnswer.equals(userAnswer);

            if (isCorrect) {
                correctCount++;
            } else {
                // 记录错题
                WrongQuestion wq = wrongQuestionMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WrongQuestion>()
                                .eq(WrongQuestion::getUserId, userId)
                                .eq(WrongQuestion::getQuestionId, questionId));
                if (wq != null) {
                    wq.setWrongAnswer(userAnswer);
                    wq.setWrongCount(wq.getWrongCount() + 1);
                    wrongQuestionMapper.updateById(wq);
                } else {
                    wq = new WrongQuestion();
                    wq.setUserId(userId);
                    wq.setQuestionId(questionId);
                    wq.setWrongAnswer(userAnswer);
                    wq.setWrongCount(1);
                    wq.setCreateTime(LocalDateTime.now());
                    wrongQuestionMapper.insert(wq);
                }
            }
        }

        double correctRate = total > 0 ? (double) correctCount / total * 100 : 0;
        int masteryLevel = correctRate >= 80 ? 3 : (correctRate >= 60 ? 2 : 1);
        if (nodeId != null) {
            studyRecordService.updateRecord(userId, nodeId, masteryLevel, BigDecimal.valueOf(correctRate), null);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("correctCount", correctCount);
        result.put("correctRate", Math.round(correctRate * 100.0) / 100.0);
        result.put("masteryLevel", masteryLevel);
        result.put("message", correctRate >= 80 ? "表现优秀！" : (correctRate >= 60 ? "继续加油！" : "建议重新学习该知识点"));

        return Result.success(result);
    }
}

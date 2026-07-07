package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.dto.ExamSubmitDTO;
import com.cupk.service.ExamService;
import com.cupk.service.ExamRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学生考试控制器 —— 考试结果的查看与提交（含乱序、防重复提交）
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentExamController {

    private final ExamRecordService examRecordService;
    private final ExamService examService;

    /**
     * 获取当前学生的考试列表及成绩
     */
    @GetMapping("/exams")
    public Result<?> listMyExams() {
        Integer userId = UserContext.getUserId();
        return Result.success(examService.listForStudent(userId));
    }

    /**
     * 获取已发布测评试卷（题目已乱序）
     */
    @GetMapping("/exam-paper/{id}")
    public Result<?> examPaper(@PathVariable Integer id) {
        Map<String, Object> paper = examService.getPaper(id);
        if (paper == null) return Result.error("试卷不存在");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) paper.get("questions");
        if (questions != null) {
            // 题目乱序（Fisher-Yates shuffle）
            java.util.Random rnd = new java.util.Random();
            for (int i = questions.size() - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                Collections.swap(questions, i, j);
            }
            paper.put("questions", questions);
        }
        // 记录发卷时间戳，用于提交时校验时长
        paper.put("issuedAt", System.currentTimeMillis());
        paper.put("durationMinutes", 60); // 默认60分钟
        return Result.success(paper);
    }

    /**
     * 获取某次考试的详细结果
     */
    @GetMapping("/exam/{id}")
    public Result<?> examDetail(@PathVariable Integer id) {
        return Result.success(examRecordService.getById(id));
    }

    /**
     * 提交考试答案并计算得分（含防重复提交校验）
     *
     * 请求体示例：
     * {
     *   "examId": 1,
     *   "courseId": 1,
     *   "issuedAt": 1712345678000,
     *   "answers": [
     *     { "questionId": 1, "answer": "A" },
     *     { "questionId": 2, "answer": "B" }
     *   ]
     * }
     */
    @PostMapping("/exam/submit")
    public Result<?> submitExam(@RequestBody Map<String, Object> body) {
        Integer userId = UserContext.getUserId();
        Integer examId = (Integer) body.get("examId");
        Integer courseId = (Integer) body.get("courseId");
        Long issuedAt = body.get("issuedAt") != null ? ((Number) body.get("issuedAt")).longValue() : null;

        // 防重复提交：检查该用户是否已提交过此考试
        if (examId != null) {
            List<com.cupk.pojo.ExamRecord> existing = examRecordService.listByUser(userId);
            boolean alreadySubmitted = existing.stream().anyMatch(r -> examId.equals(r.getExamId()));
            if (alreadySubmitted) {
                return Result.error(400, "该考试已经提交过，不能重复提交");
            }
        }

        // 超时校验（发卷后超过120分钟则拒绝提交）
        if (issuedAt != null) {
            long elapsed = System.currentTimeMillis() - issuedAt;
            if (elapsed > 120 * 60 * 1000L) {
                return Result.error(400, "考试已超时，不能提交");
            }
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");

        ExamSubmitDTO dto = new ExamSubmitDTO();
        dto.setExamId(examId);
        dto.setCourseId(courseId);
        List<ExamSubmitDTO.AnswerItem> answerItems = new ArrayList<>();
        if (answers != null) {
            for (Map<String, Object> ans : answers) {
                ExamSubmitDTO.AnswerItem item = new ExamSubmitDTO.AnswerItem();
                item.setQuestionId((Integer) ans.get("questionId"));
                item.setUserAnswer((String) ans.get("answer"));
                answerItems.add(item);
            }
        }
        dto.setAnswers(answerItems);

        examRecordService.submitExam(userId, dto);
        return Result.success("考试提交成功");
    }
}

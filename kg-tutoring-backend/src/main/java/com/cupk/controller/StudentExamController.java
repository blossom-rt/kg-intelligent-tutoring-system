package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.dto.ExamSubmitDTO;
import com.cupk.service.ExamService;
import com.cupk.service.ExamRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 学生考试控制器 —— 考试结果的查看与提交
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
     * 获取已发布测评试卷
     */
    @GetMapping("/exam-paper/{id}")
    public Result<?> examPaper(@PathVariable Integer id) {
        return Result.success(examService.getPaper(id));
    }

    /**
     * 获取某次考试的详细结果
     */
    @GetMapping("/exam/{id}")
    public Result<?> examDetail(@PathVariable Integer id) {
        return Result.success(examRecordService.getById(id));
    }

    /**
     * 提交考试答案并计算得分
     *
     * 请求体示例：
     * {
     *   "courseId": 1,
     *   "answers": [
     *     { "questionId": 1, "answer": "A" },
     *     { "questionId": 2, "answer": "B" }
     *   ]
     * }
     *
     * TODO: 后续需要加入防作弊机制、考试时间限制、题目乱序等
     */
    @PostMapping("/exam/submit")
    public Result<?> submitExam(@RequestBody Map<String, Object> body) {
        Integer userId = UserContext.getUserId();
        Integer examId = (Integer) body.get("examId");
        Integer courseId = (Integer) body.get("courseId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");

        // 构建 ExamSubmitDTO
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

        // 提交考试（错题记录由 Service 层内部处理）
        examRecordService.submitExam(userId, dto);
        return Result.success("考试提交成功");
    }
}

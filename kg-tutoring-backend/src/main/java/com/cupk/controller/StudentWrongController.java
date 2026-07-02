package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生错题本控制器 —— 错题的查看与移除
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentWrongController {

    private final WrongQuestionService wrongQuestionService;

    /**
     * 获取当前学生的错题列表
     */
    @GetMapping("/wrong-questions")
    public Result<?> listWrongQuestions() {
        Integer userId = UserContext.getUserId();
        return Result.success(wrongQuestionService.listByUser(userId));
    }

    /**
     * 从错题本中移除某道错题（已掌握后手动移除）
     */
    @DeleteMapping("/wrong-questions/{id}")
    public Result<?> removeWrongQuestion(@PathVariable Integer id) {
        wrongQuestionService.remove(id, UserContext.getUserId());
        return Result.success("错题已移除");
    }
}

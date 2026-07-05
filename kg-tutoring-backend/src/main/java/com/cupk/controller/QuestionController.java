package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.Question;
import com.cupk.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器 —— 题目的增删改查（教师操作）
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 检查当前用户是否为教师，否则抛出无权限异常
     */
    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询题目列表，可按知识点节点筛选
     *
     * @param nodeId 知识点节点 ID（可选）
     */
    @GetMapping
    public Result<List<Question>> list(
            @RequestParam(required = false) Integer nodeId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer difficulty) {
        return Result.success(questionService.list(nodeId, courseId, difficulty));
    }

    /**
     * 新增题目（仅教师）
     */
    @OperLog(module = "题库管理", operation = "新增题目")
    @PostMapping
    public Result<?> create(@RequestBody Question question) {
        checkTeacher();
        questionService.save(question);
        return Result.success("新增题目成功");
    }

    /**
     * 修改题目（仅教师）
     */
    @OperLog(module = "题库管理", operation = "修改题目")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Question question) {
        checkTeacher();
        question.setId(id);
        questionService.update(question);
        return Result.success("修改题目成功");
    }

    /**
     * 删除题目（仅教师）
     */
    @OperLog(module = "题库管理", operation = "删除题目")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        questionService.delete(id);
        return Result.success("删除题目成功");
    }
}

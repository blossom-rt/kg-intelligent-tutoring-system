package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.Question;
import com.cupk.pojo.WrongQuestion;
import com.cupk.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学生错题本控制器 —— 错题的查看与移除
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentWrongController {

    private final WrongQuestionService wrongQuestionService;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;

    /**
     * 获取当前学生的错题列表（含题目内容和知识点名称）
     */
    @GetMapping("/wrong-questions")
    public Result<List<Map<String, Object>>> listWrongQuestions() {
        Integer userId = UserContext.getUserId();
        List<WrongQuestion> wrongList = wrongQuestionService.listByUser(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WrongQuestion wq : wrongList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", wq.getId());
            item.put("questionId", wq.getQuestionId());
            item.put("wrongAnswer", wq.getWrongAnswer());
            item.put("wrongCount", wq.getWrongCount());
            item.put("createTime", wq.getCreateTime());

            // 补充题目内容
            Question question = questionMapper.selectById(wq.getQuestionId());
            if (question != null) {
                item.put("content", question.getContent());
                item.put("answer", question.getAnswer());
                item.put("analysis", question.getAnalysis());
                // 补充知识点名称
                KnowledgeNode node = knowledgeNodeMapper.selectById(question.getNodeId());
                item.put("nodeName", node != null ? node.getName() : "未知");
            } else {
                item.put("content", "题目已删除");
                item.put("nodeName", "未知");
            }
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 添加错题（练习时答错自动收录）
     */
    @PostMapping("/wrong-questions")
    public Result<?> addWrongQuestion(@RequestBody Map<String, Object> body) {
        Integer userId = UserContext.getUserId();
        Integer questionId = (Integer) body.get("questionId");
        String wrongAnswer = (String) body.get("wrongAnswer");
        // 检查是否已存在
        List<WrongQuestion> existing = wrongQuestionService.listByUser(userId);
        WrongQuestion exist = existing.stream()
                .filter(w -> w.getQuestionId().equals(questionId))
                .findFirst().orElse(null);
        if (exist != null) {
            exist.setWrongCount(exist.getWrongCount() + 1);
            exist.setWrongAnswer(wrongAnswer);
            wrongQuestionMapper.updateById(exist);
        } else {
            WrongQuestion wq = new WrongQuestion();
            wq.setUserId(userId);
            wq.setQuestionId(questionId);
            wq.setWrongAnswer(wrongAnswer);
            wq.setWrongCount(1);
            wrongQuestionMapper.insert(wq);
        }
        return Result.success("已收录错题");
    }

    /**
     * 从错题本中移除某道错题
     */
    @DeleteMapping("/wrong-questions/{id}")
    public Result<?> removeWrongQuestion(@PathVariable Integer id) {
        wrongQuestionService.remove(id, UserContext.getUserId());
        return Result.success("错题已移除");
    }
}

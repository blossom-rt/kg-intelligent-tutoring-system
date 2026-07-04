package com.cupk.controller;

import com.cupk.ai.DeepSeekService;
import com.cupk.common.Result;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.pojo.ExamRecord;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 学生 AI 助手控制器 —— 调用 DeepSeek 生成智能内容
 */
@RestController
@RequestMapping("/api/student/ai")
@RequiredArgsConstructor
public class StudentAiController {

    private final DeepSeekService deepSeekService;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final QuestionMapper questionMapper;
    private final ExamRecordMapper examRecordMapper;

    @PostMapping("/node-summary")
    public Result<Map<String, Object>> nodeSummary(@RequestBody Map<String, Object> body) {
        Integer nodeId = (Integer) body.get("nodeId");
        KnowledgeNode node = knowledgeNodeMapper.selectById(nodeId);
        if (node == null) return Result.error("知识点不存在");

        String userPrompt = "请为以下知识点生成一份学习总结，包含核心概念、学习要点和掌握目标。\n\n"
                + "知识点名称：" + node.getName() + "\n"
                + "难度等级：" + (node.getDifficulty() != null ? node.getDifficulty() : "中等") + "\n"
                + "所属章节：" + (node.getChapter() != null ? node.getChapter() : "无") + "\n"
                + "内容描述：" + (node.getDescription() != null ? node.getDescription() : "无") + "\n"
                + "建议时长：" + (node.getExpectedMinutes() != null ? node.getExpectedMinutes() : 30) + " 分钟";

        String aiResult = deepSeekService.generate("你是一个专业的学科教师，擅长用通俗易懂的方式总结知识点，帮助学生快速理解和掌握。", userPrompt);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodeId", node.getId());
        result.put("title", node.getName() + " - AI 学习总结");
        result.put("summary", aiResult);
        result.put("difficulty", node.getDifficulty());
        return Result.success(result);
    }

    @PostMapping("/wrong-explain")
    public Result<Map<String, Object>> wrongExplain(@RequestBody Map<String, Object> body) {
        Integer questionId = (Integer) body.get("questionId");
        Question question = questionMapper.selectById(questionId);
        if (question == null) return Result.error("题目不存在");

        String userPrompt = "请为以下错题生成详细的讲解，包含解题思路、错误原因分析和举一反三的建议。\n\n"
                + "题目：" + question.getContent() + "\n"
                + "正确答案：" + question.getAnswer() + "\n"
                + "题目解析：" + (question.getAnalysis() != null ? question.getAnalysis() : "无") + "\n"
                + "题型：" + (question.getQuestionType() != null ? question.getQuestionType() : "未知") + "\n"
                + "难度：" + (question.getDifficulty() != null ? question.getDifficulty() : "中等");

        String aiResult = deepSeekService.generate("你是一个耐心的学科辅导老师，擅长帮助学生分析错题原因，用易于理解的方式讲解题目。", userPrompt);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", "AI 错题讲解");
        result.put("questionContent", question.getContent());
        result.put("correctAnswer", question.getAnswer());
        result.put("analysis", question.getAnalysis());
        result.put("aiExplain", aiResult);
        return Result.success(result);
    }

    @PostMapping("/exam-report")
    public Result<Map<String, Object>> examReport(@RequestBody Map<String, Object> body) {
        Integer examId = (Integer) body.get("examId");
        ExamRecord record = examRecordMapper.selectById(examId);
        if (record == null) return Result.error("测评记录不存在");

        double score = record.getUserScore() != null ? record.getUserScore().doubleValue() : 0;
        double total = record.getTotalScore() != null ? record.getTotalScore().doubleValue() : 1;
        double rate = total > 0 ? score / total * 100 : 0;

        String userPrompt = "请为以下测评成绩生成一份诊断报告，包含成绩分析、薄弱环节诊断和后续学习建议。\n\n"
                + "得分：" + score + " / " + total + "（" + String.format("%.1f", rate) + "%）\n"
                + (rate >= 60 ? "状态：已通过" : "状态：未通过");

        String aiResult = deepSeekService.generate("你是一个经验丰富的学业诊断分析师，善于根据测评成绩为学生提供个性化的学习建议和改进方案。", userPrompt);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", "AI 测评诊断报告");
        result.put("totalScore", record.getTotalScore());
        result.put("userScore", record.getUserScore());
        result.put("passed", rate >= 60);
        result.put("aiReport", aiResult);
        return Result.success(result);
    }
}

package com.cupk.controller;

import com.cupk.ai.DeepSeekService;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.AiCallLogMapper;
import com.cupk.service.KnowledgeNodeService;
import com.cupk.mapper.QuestionMapper;
import com.cupk.pojo.AiCallLog;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 学生 AI 助手控制器 —— 调用 DeepSeek 生成智能内容
 */
@RestController
@RequestMapping("/api/student/ai")
@RequiredArgsConstructor
@Slf4j
public class StudentAiController {

    private final DeepSeekService deepSeekService;
    private final KnowledgeNodeService knowledgeNodeService;
    private final QuestionMapper questionMapper;
    private final AiCallLogMapper aiCallLogMapper;

    @PostMapping("/node-summary")
    public Result<Map<String, Object>> nodeSummary(@RequestBody Map<String, Object> body) {
        long start = System.currentTimeMillis();
        Integer nodeId = (Integer) body.get("nodeId");
        KnowledgeNode node = knowledgeNodeService.getById(nodeId);
        if (node == null) return Result.error("知识点不存在");

        String userPrompt = "请为以下知识点生成一份学习总结，包含核心概念、学习要点和掌握目标。不要使用 LaTeX 语法（反斜杠加符号的形式），数学符号用 Unicode 或普通文本表示。\n\n"
                + "知识点名称：" + node.getName() + "\n"
                + "难度等级：" + (node.getDifficulty() != null ? node.getDifficulty() : "中等") + "\n"
                + "所属章节：" + (node.getChapterName() != null ? node.getChapterName() : "无") + "\n"
                + "内容描述：" + (node.getDescription() != null ? node.getDescription() : "无") + "\n"
                + "建议时长：" + (node.getExpectedMinutes() != null ? node.getExpectedMinutes() : 30) + " 分钟";

        String aiResult = deepSeekService.generate("你是一个专业的学科教师，擅长用通俗易懂的方式总结知识点，帮助学生快速理解和掌握。", userPrompt);
        saveLog("node-summary", userPrompt, aiResult, start);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodeId", node.getId());
        result.put("title", node.getName() + " - AI 学习总结");
        result.put("summary", aiResult);
        result.put("difficulty", node.getDifficulty());
        return Result.success(result);
    }

    @PostMapping("/wrong-explain")
    public Result<Map<String, Object>> wrongExplain(@RequestBody Map<String, Object> body) {
        long start = System.currentTimeMillis();
        Integer questionId = (Integer) body.get("questionId");
        Question question = questionMapper.selectById(questionId);
        if (question == null) return Result.error("题目不存在");

        String userPrompt = "请为以下错题生成详细的讲解，包含解题思路、错误原因分析和举一反三的建议。不要使用 LaTeX 语法（反斜杠加符号的形式），数学符号用 Unicode 或普通文本表示。\n\n"
                + "题目：" + question.getContent() + "\n"
                + "正确答案：" + question.getAnswer() + "\n"
                + "题目解析：" + (question.getAnalysis() != null ? question.getAnalysis() : "无") + "\n"
                + "题型：" + (question.getQuestionType() != null ? question.getQuestionType() : "未知") + "\n"
                + "难度：" + (question.getDifficulty() != null ? question.getDifficulty() : "中等");

        String aiResult = deepSeekService.generate("你是一个耐心的学科辅导老师，擅长帮助学生分析错题原因，用易于理解的方式讲解题目。", userPrompt);
        saveLog("wrong-explain", userPrompt, aiResult, start);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", "AI 错题讲解");
        result.put("questionContent", question.getContent());
        result.put("correctAnswer", question.getAnswer());
        result.put("analysis", question.getAnalysis());
        result.put("aiExplain", aiResult);
        return Result.success(result);
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        long start = System.currentTimeMillis();
        Integer nodeId = (Integer) body.get("nodeId");
        String question = (String) body.get("question");
        if (question == null || question.isBlank()) {
            return Result.error("请输入问题");
        }

        String nodeContext = "";
        if (nodeId != null) {
            KnowledgeNode node = knowledgeNodeService.getById(nodeId);
            if (node != null) {
                nodeContext = "\n\n相关知识点：" + (node.getName() != null ? node.getName() : "")
                        + "\n内容：" + (node.getDescription() != null ? node.getDescription() : "");
            }
        }

        String userPrompt = "请回答学生的以下问题，要求讲解清晰、通俗易懂。不要使用 LaTeX 语法（反斜杠加符号的形式），数学符号用 Unicode 或普通文本表示。" + nodeContext + "\n\n学生提问：" + question;
        String aiResult = deepSeekService.generate("你是一名称职的学科辅导老师，耐心细致，善于用通俗易懂的方式解答学生疑问。", userPrompt);
        saveLog("chat", userPrompt, aiResult, start);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", aiResult);
        return Result.success(result);
    }

    private void saveLog(String scene, String prompt, String result, long startTime) {
        try {
            AiCallLog log = new AiCallLog();
            Integer uid = UserContext.getUserId();
            if (uid != null) log.setUserId(uid);
            log.setScene(scene);
            log.setPrompt(prompt.length() > 500 ? prompt.substring(0, 500) : prompt);
            log.setResult(result.length() > 500 ? result.substring(0, 500) : result);
            log.setCallDuration((int) (System.currentTimeMillis() - startTime));
            log.setStatus(1);
            log.setCreateTime(LocalDateTime.now());
            aiCallLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存AI日志失败", e);
        }
    }
}

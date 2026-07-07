package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.dto.ExamSubmitDTO;
import com.cupk.ai.DeepSeekService;
import com.cupk.mapper.ExamMapper;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.Exam;
import com.cupk.pojo.ExamRecord;
import com.cupk.pojo.Question;
import com.cupk.pojo.WrongQuestion;
import com.cupk.service.ExamRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 测评记录服务实现
 */
@Service
@RequiredArgsConstructor
public class ExamRecordServiceImpl implements ExamRecordService {

    private final ExamRecordMapper examRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final DeepSeekService deepSeekService;

    @Override
    public void submitExam(Integer userId, ExamSubmitDTO dto) {
        List<ExamSubmitDTO.AnswerItem> answers = dto.getAnswers();

        // 计算得分
        int totalScore = 0;
        int userScore = 0;
        StringBuilder wrongDetails = new StringBuilder();

        for (ExamSubmitDTO.AnswerItem item : answers) {
            Question question = questionMapper.selectById(item.getQuestionId());
            if (question != null) {
                totalScore++;
                if (question.getAnswer() != null && question.getAnswer().equals(item.getUserAnswer())) {
                    userScore++;
                } else {
                    // 记录错题详情供 AI 分析
                    wrongDetails.append("- 题目：").append(question.getContent()).append("\n")
                            .append("  正确答案：").append(question.getAnswer()).append("\n")
                            .append("  你的答案：").append(item.getUserAnswer()).append("\n");
                    if (question.getAnalysis() != null) {
                        wrongDetails.append("  解析：").append(question.getAnalysis()).append("\n");
                    }
                    // 保存错题
                    WrongQuestion existing = wrongQuestionMapper.selectOne(
                            new LambdaQueryWrapper<WrongQuestion>()
                                    .eq(WrongQuestion::getUserId, userId)
                                    .eq(WrongQuestion::getQuestionId, item.getQuestionId()));

                    if (existing != null) {
                        existing.setWrongAnswer(item.getUserAnswer());
                        existing.setWrongCount(existing.getWrongCount() + 1);
                        wrongQuestionMapper.updateById(existing);
                    } else {
                        WrongQuestion wq = new WrongQuestion();
                        wq.setUserId(userId);
                        wq.setQuestionId(item.getQuestionId());
                        wq.setWrongAnswer(item.getUserAnswer());
                        wq.setWrongCount(1);
                        wq.setCreateTime(LocalDateTime.now());
                        wrongQuestionMapper.insert(wq);
                    }
                }
            }
        }

        // 保存测评记录
        Exam exam = dto.getExamId() != null ? examMapper.selectById(dto.getExamId()) : null;
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(dto.getExamId());
        record.setCourseId(exam != null ? exam.getCourseId() : dto.getCourseId());
        record.setTotalScore(exam != null && exam.getTotalScore() != null ? exam.getTotalScore() : totalScore);
        BigDecimal score = totalScore > 0 && record.getTotalScore() != null
                ? BigDecimal.valueOf(userScore)
                    .multiply(BigDecimal.valueOf(record.getTotalScore()))
                    .divide(BigDecimal.valueOf(totalScore), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        record.setUserScore(score);
        try {
            String wrongDetail = wrongDetails.length() > 0
                    ? "\n\n【错题详情】\n" + wrongDetails.toString().substring(0, Math.min(wrongDetails.length(), 2000))
                    : "\n\n本次答题全部正确，无错题。";
            String prompt = "请为以下测评成绩生成一份诊断报告，包含成绩分析、薄弱环节诊断和后续学习建议。\n\n"
                    + "得分：" + userScore + " / " + totalScore
                    + "（" + (totalScore > 0 ? String.format("%.1f", userScore * 100.0 / totalScore) : "0") + "%）"
                    + "\n共 " + totalScore + " 题，答对 " + userScore + " 题，答错 " + (totalScore - userScore) + " 题。"
                    + wrongDetail;
            String aiReport = deepSeekService.generate(
                    "你是一个经验丰富的学业诊断分析师，善于根据测评成绩为学生提供个性化的学习建议。",
                    prompt);
            record.setAiReport(aiReport != null ? aiReport : "测评完成，共 " + totalScore + " 题，答对 " + userScore + " 题。");
        } catch (Exception e) {
            record.setAiReport("测评完成，共 " + totalScore + " 题，答对 " + userScore + " 题，得分 " + score + " 分。");
        }
        record.setCreateTime(LocalDateTime.now());
        examRecordMapper.insert(record);
    }

    @Override
    public List<ExamRecord> listByUser(Integer userId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .orderByDesc(ExamRecord::getCreateTime));
    }

    @Override
    public Map<String, Object> getById(Integer id) {
        ExamRecord record = examRecordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        Exam exam = record.getExamId() != null ? examMapper.selectById(record.getExamId()) : null;
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", record.getId());
        result.put("examId", record.getExamId());
        result.put("examName", exam != null ? exam.getExamName() : "阶段测评");
        result.put("courseId", record.getCourseId());
        result.put("totalScore", record.getTotalScore());
        result.put("score", record.getUserScore());
        result.put("userScore", record.getUserScore());
        result.put("passed", record.getUserScore() != null
                && record.getTotalScore() != null
                && record.getUserScore().doubleValue() >= record.getTotalScore() * 0.6);
        result.put("aiReport", record.getAiReport());
        result.put("createTime", record.getCreateTime());
        return result;
    }
}

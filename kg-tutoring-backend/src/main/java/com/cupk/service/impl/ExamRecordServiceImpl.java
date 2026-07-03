package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.dto.ExamSubmitDTO;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.ExamRecord;
import com.cupk.pojo.Question;
import com.cupk.pojo.WrongQuestion;
import com.cupk.service.ExamRecordService;
import com.cupk.service.QuestionService;
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

    @Override
    public void submitExam(Integer userId, ExamSubmitDTO dto) {
        List<ExamSubmitDTO.AnswerItem> answers = dto.getAnswers();

        // 计算得分
        int totalScore = 0;
        int userScore = 0;

        for (ExamSubmitDTO.AnswerItem item : answers) {
            Question question = questionMapper.selectById(item.getQuestionId());
            if (question != null) {
                totalScore++;
                if (question.getAnswer() != null && question.getAnswer().equals(item.getUserAnswer())) {
                    userScore++;
                } else {
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
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setCourseId(dto.getCourseId());
        record.setTotalScore(totalScore);
        record.setUserScore(new BigDecimal(userScore));
        // TODO: 接入 AI 生成诊断报告
        record.setAiReport("测评完成，共 " + totalScore + " 题，答对 " + userScore + " 题");
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
    public ExamRecord getById(Integer id) {
        return examRecordMapper.selectById(id);
    }

    // ===== 教师管理方法 =====

    @Override
    public List<ExamRecord> listByCourse(Integer courseId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getCourseId, courseId)
                        .orderByDesc(ExamRecord::getCreateTime));
    }

    @Override
    public void createExam(Integer courseId, List<Integer> questionIds, Integer totalScore) {
        // 目前 ExamRecord 只存最终结果，用 score 字段标注题量便于演示
        ExamRecord exam = new ExamRecord();
        exam.setCourseId(courseId);
        exam.setTotalScore(totalScore != null ? totalScore : questionIds.size());
        exam.setUserScore(BigDecimal.ZERO);
        exam.setCreateTime(LocalDateTime.now());
        examRecordMapper.insert(exam);
    }

    @Override
    public void updateExam(Integer id, Map<String, Object> body) {
        ExamRecord exam = examRecordMapper.selectById(id);
        if (exam == null) return;
        if (body.containsKey("totalScore")) {
            exam.setTotalScore((Integer) body.get("totalScore"));
        }
        examRecordMapper.updateById(exam);
    }

    @Override
    public void deleteExam(Integer id) {
        examRecordMapper.deleteById(id);
    }
}

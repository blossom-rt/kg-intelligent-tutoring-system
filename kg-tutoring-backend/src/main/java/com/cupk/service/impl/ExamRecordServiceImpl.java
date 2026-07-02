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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
}

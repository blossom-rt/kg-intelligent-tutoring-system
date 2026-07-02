package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.StudyRecordMapper;
import com.cupk.pojo.StudyRecord;
import com.cupk.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学习记录服务实现
 */
@Service
@RequiredArgsConstructor
public class StudyRecordServiceImpl implements StudyRecordService {

    private final StudyRecordMapper studyRecordMapper;

    @Override
    public StudyRecord getByUserAndNode(Integer userId, Integer nodeId) {
        return studyRecordMapper.selectOne(
                new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId)
                        .eq(StudyRecord::getNodeId, nodeId));
    }

    @Override
    public void updateRecord(Integer userId, Integer nodeId, Integer masteryLevel,
                             BigDecimal correctRate, Integer studyMinutes) {
        // 查询已有记录：存在则更新，不存在则新增
        StudyRecord record = studyRecordMapper.selectOne(
                new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId)
                        .eq(StudyRecord::getNodeId, nodeId));

        if (record != null) {
            record.setMasteryLevel(masteryLevel);
            record.setCorrectRate(correctRate);
            record.setStudyMinutes(studyMinutes);
            record.setUpdateTime(LocalDateTime.now());
            studyRecordMapper.updateById(record);
        } else {
            record = new StudyRecord();
            record.setUserId(userId);
            record.setNodeId(nodeId);
            record.setMasteryLevel(masteryLevel);
            record.setCorrectRate(correctRate);
            record.setStudyMinutes(studyMinutes);
            record.setUpdateTime(LocalDateTime.now());
            studyRecordMapper.insert(record);
        }
    }
}

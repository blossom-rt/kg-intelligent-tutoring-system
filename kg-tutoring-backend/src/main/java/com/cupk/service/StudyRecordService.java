package com.cupk.service;

import com.cupk.pojo.StudyRecord;

import java.math.BigDecimal;

/**
 * 学习记录服务
 */
public interface StudyRecordService {

    /** 查询用户在某个知识点上的学习记录 */
    StudyRecord getByUserAndNode(Integer userId, Integer nodeId);

    /** 更新或创建学习记录 */
    void updateRecord(Integer userId, Integer nodeId, Integer masteryLevel, BigDecimal correctRate, Integer studyMinutes);
}

package com.cupk.service;

import com.cupk.dto.ExamSubmitDTO;
import com.cupk.pojo.ExamRecord;

import java.util.List;

/**
 * 测评记录服务
 */
public interface ExamRecordService {

    /** 提交测评 */
    void submitExam(Integer userId, ExamSubmitDTO dto);

    /** 查询某用户的测评记录列表 */
    List<ExamRecord> listByUser(Integer userId);

    /** 根据ID查询测评记录 */
    ExamRecord getById(Integer id);
}

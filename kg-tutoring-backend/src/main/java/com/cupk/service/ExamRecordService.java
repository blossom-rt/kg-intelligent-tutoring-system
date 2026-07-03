package com.cupk.service;

import com.cupk.dto.ExamSubmitDTO;
import com.cupk.pojo.ExamRecord;

import java.util.List;
import java.util.Map;

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

    // ===== 教师管理方法 =====

    /** 按课程查询测评列表 */
    List<ExamRecord> listByCourse(Integer courseId);

    /** 创建测评（含题目选取） */
    void createExam(Integer courseId, List<Integer> questionIds, Integer totalScore);

    /** 更新测评 */
    void updateExam(Integer id, Map<String, Object> body);

    /** 删除测评 */
    void deleteExam(Integer id);
}

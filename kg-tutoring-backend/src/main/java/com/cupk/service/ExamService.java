package com.cupk.service;

import java.util.List;
import java.util.Map;

/**
 * 测评定义服务
 */
public interface ExamService {

    /** 教师查询测评列表 */
    List<Map<String, Object>> listForTeacher(Integer courseId);

    /** 创建测评 */
    void createExam(String examName, Integer courseId, List<Integer> questionIds, Integer totalScore);

    /** 修改测评 */
    void updateExam(Integer id, Map<String, Object> body);

    /** 删除测评 */
    void deleteExam(Integer id);

    /** 学生查询已发布测评及完成状态 */
    List<Map<String, Object>> listForStudent(Integer userId);

    /** 学生获取试卷题目 */
    Map<String, Object> getPaper(Integer examId);
}

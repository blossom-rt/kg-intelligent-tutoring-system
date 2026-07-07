package com.cupk.service;

import java.util.Map;

/**
 * 数据分析服务（教师/学生统计）
 */
public interface AnalysisService {

    /** 班级整体分析（按课程） */
    Map<String, Object> classAnalysis(Integer courseId);

    /** 个人学习分析 */
    Map<String, Object> personalAnalysis(Integer userId);

    /** 薄弱知识点分析 */
    Map<String, Object> weakAnalysis(Integer userId);
}

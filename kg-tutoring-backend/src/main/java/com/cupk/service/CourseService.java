package com.cupk.service;

import com.cupk.pojo.Course;

import java.util.List;
import java.util.Map;

/**
 * 课程服务
 */
public interface CourseService {

    /** 查询课程列表（可按学科、教师过滤） */
    List<Course> list(String subject, Integer teacherId);

    /** 根据ID查询课程 */
    Course getById(Integer id);

    /** 推荐与指定课程相关度高的其他课程 */
    List<Map<String, Object>> recommendRelatedCourses(Integer courseId, Integer limit);

    /** 推荐学习该课程的同学还学习的其他课程 */
    List<Map<String, Object>> recommendAlsoLearnedCourses(Integer courseId, Integer limit);

    /** 新增课程 */
    void save(Course course);

    /** 更新课程 */
    void update(Course course);

    /** 删除课程 */
    void delete(Integer id);
}

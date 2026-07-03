package com.cupk.service;

import com.cupk.pojo.Course;

import java.util.List;

/**
 * 课程服务
 */
public interface CourseService {

    /** 查询课程列表 */
    List<Course> list(String subject);

    /** 根据ID查询课程 */
    Course getById(Integer id);

    /** 新增课程 */
    void save(Course course);

    /** 更新课程 */
    void update(Course course);

    /** 删除课程 */
    void delete(Integer id);
}

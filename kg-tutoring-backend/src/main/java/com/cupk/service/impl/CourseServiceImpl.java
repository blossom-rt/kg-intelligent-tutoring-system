package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.CourseMapper;
import com.cupk.pojo.Course;
import com.cupk.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 课程服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public List<Course> list(String subject, Integer teacherId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .orderByDesc(Course::getCreateTime);
        if (subject != null && !subject.isEmpty()) {
            wrapper.like(Course::getSubject, subject);
        }
        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        }
        return courseMapper.selectList(wrapper);
    }

    @Override
    public Course getById(Integer id) {
        return courseMapper.selectById(id);
    }

    @Override
    public List<Map<String, Object>> recommendRelatedCourses(Integer courseId, Integer limit) {
        return courseMapper.selectRelatedCourses(courseId, normalizeLimit(limit));
    }

    @Override
    public List<Map<String, Object>> recommendAlsoLearnedCourses(Integer courseId, Integer limit) {
        return courseMapper.selectAlsoLearnedCourses(courseId, normalizeLimit(limit));
    }

    @Override
    public void save(Course course) {
        courseMapper.insert(course);
    }

    @Override
    public void update(Course course) {
        courseMapper.updateById(course);
    }

    @Override
    public void delete(Integer id) {
        courseMapper.deleteById(id);
    }

    private Integer normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 6;
        }
        return Math.min(limit, 20);
    }
}

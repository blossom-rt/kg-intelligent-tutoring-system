package com.cupk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.common.Result;
import com.cupk.mapper.CourseMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.StudyRecordMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.StudyRecord;
import com.cupk.pojo.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 教师首页控制器 —— 提供首页统计看板数据
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherHomeController {

    private final CourseMapper courseMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final QuestionMapper questionMapper;
    private final SysUserMapper sysUserMapper;
    private final StudyRecordMapper studyRecordMapper;

    /**
     * 教师首页数据看板
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        // 课程总数
        long courseCount = courseMapper.selectCount(null);

        // 学生总数（role_id = 3 为学生角色）
        long studentCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleId, 3));

        // 知识点总数
        long nodeCount = knowledgeNodeMapper.selectCount(null);

        // 题目总数
        long questionCount = questionMapper.selectCount(null);

        // 本周学习人次
        long weekStudy = studyRecordMapper.selectCount(
                new LambdaQueryWrapper<StudyRecord>()
                        .ge(StudyRecord::getUpdateTime, java.time.LocalDateTime.now().minusDays(7)));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", courseCount);
        stats.put("activeStudents", studentCount);
        stats.put("weekStudy", weekStudy);
        stats.put("totalNodes", nodeCount);
        stats.put("totalQuestions", questionCount);

        return Result.success(stats);
    }
}

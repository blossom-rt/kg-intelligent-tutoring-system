package com.cupk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.PathDetailMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.pojo.PathDetail;
import com.cupk.pojo.StudyPath;
import com.cupk.pojo.SysUser;
import com.cupk.service.AnalysisService;
import com.cupk.service.StudyPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 教师分析控制器 —— 班级/课程维度的学情分析（教师操作）
 */
@RestController
@RequestMapping("/api/teacher/analysis")
@RequiredArgsConstructor
public class TeacherAnalysisController {

    private final AnalysisService analysisService;
    private final StudyPathService studyPathService;
    private final PathDetailMapper pathDetailMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 检查当前用户是否为教师，否则抛出无权限异常
     */
    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 班级整体学情分析
     *
     * @param courseId 课程 ID
     * @return 包含班级整体掌握情况、各知识点平均掌握度、成绩分布、学习趋势、薄弱排行等数据
     */
    @GetMapping("/class")
    public Result<?> classAnalysis(@RequestParam Integer courseId) {
        checkTeacher();
        return Result.success(analysisService.classAnalysis(courseId));
    }

    /**
     * 获取学生列表（供教师选择）
     */
    @GetMapping("/students")
    public Result<List<Map<String, Object>>> listStudents() {
        checkTeacher();
        List<SysUser> students = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRoleId, 3)
                        .orderByAsc(SysUser::getId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser s : students) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("username", s.getUsername());
            item.put("realName", s.getRealName());
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 查询指定学生的学习路径列表（含进度）
     *
     * @param studentId 学生用户ID
     * @return 该学生的所有学习路径及进度
     */
    @GetMapping("/student-paths")
    public Result<?> studentPaths(@RequestParam Integer studentId) {
        checkTeacher();

        // 查询学生信息，用于返回学生名称
        SysUser student = sysUserMapper.selectById(studentId);

        List<StudyPath> paths = studyPathService.listByUser(studentId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (StudyPath p : paths) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("pathName", p.getPathName());
            item.put("targetNodeId", p.getTargetNodeId());
            item.put("totalNodes", p.getTotalNodes());
            item.put("createTime", p.getCreateTime());
            item.put("aiSummary", p.getAiSummary());

            // 计算进度：已完成节点数 / 总节点数
            long finishedNodes = pathDetailMapper.selectCount(
                    new LambdaQueryWrapper<PathDetail>()
                            .eq(PathDetail::getPathId, p.getId())
                            .eq(PathDetail::getIsFinished, 1));
            int progress = p.getTotalNodes() != null && p.getTotalNodes() > 0
                    ? (int) Math.round(finishedNodes * 100.0 / p.getTotalNodes())
                    : 0;
            item.put("progress", progress);

            // 状态转换：0=学习中, 1=已完成
            String status = p.getStatus() != null && p.getStatus() == 1 ? "completed" : "active";
            item.put("status", status);

            result.add(item);
        }

        // 包装结果：附带学生基本信息
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student != null ? student.getRealName() : "未知学生");
        response.put("paths", result);

        return Result.success(response);
    }
}

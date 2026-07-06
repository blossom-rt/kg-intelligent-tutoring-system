package com.cupk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.PathDetailMapper;
import com.cupk.mapper.StudyPathMapper;
import com.cupk.mapper.StudyRecordMapper;
import com.cupk.pojo.PathDetail;
import com.cupk.pojo.StudyPath;
import com.cupk.pojo.StudyRecord;
import com.cupk.service.KnowledgeEdgeService;
import com.cupk.service.KnowledgeNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生知识图谱控制器 —— 展示知识图谱的节点与边数据
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentGraphController {

    private final KnowledgeNodeService knowledgeNodeService;
    private final KnowledgeEdgeService knowledgeEdgeService;
    private final StudyRecordMapper studyRecordMapper;
    private final StudyPathMapper studyPathMapper;
    private final PathDetailMapper pathDetailMapper;

    /**
     * 获取知识图谱数据（节点 + 边），可按课程筛选
     *
     * @param courseId 课程 ID（可选）
     */
    @GetMapping("/graph")
    public Result<Map<String, Object>> graph(@RequestParam(required = false) Integer courseId) {
        Integer userId = UserContext.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("nodes", knowledgeNodeService.listByCourse(courseId));
        data.put("edges", knowledgeEdgeService.listAll());
        data.put("learnedNodeIds", getLearnedNodeIds(userId));
        data.put("pathNodeIds", getPathNodeIds(userId));
        data.put("pathGroups", getPathGroups(userId));
        return Result.success(data);
    }

    private Set<Integer> getLearnedNodeIds(Integer userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        List<StudyRecord> records = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId));
        return records.stream()
                .filter(r -> (r.getMasteryLevel() != null && r.getMasteryLevel() > 0)
                        || (r.getStudyMinutes() != null && r.getStudyMinutes() > 0))
                .map(StudyRecord::getNodeId)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getPathNodeIds(Integer userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        List<StudyPath> paths = studyPathMapper.selectList(
                new LambdaQueryWrapper<StudyPath>()
                        .eq(StudyPath::getUserId, userId));
        if (paths.isEmpty()) {
            return Collections.emptySet();
        }
        List<Integer> pathIds = paths.stream().map(StudyPath::getId).toList();
        List<PathDetail> details = pathDetailMapper.selectList(
                new LambdaQueryWrapper<PathDetail>()
                        .in(PathDetail::getPathId, pathIds));
        return details.stream()
                .map(PathDetail::getNodeId)
                .collect(Collectors.toSet());
    }

    private List<Map<String, Object>> getPathGroups(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<StudyPath> paths = studyPathMapper.selectList(
                new LambdaQueryWrapper<StudyPath>()
                        .eq(StudyPath::getUserId, userId));
        if (paths.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> pathIds = paths.stream().map(StudyPath::getId).toList();
        List<PathDetail> details = pathDetailMapper.selectList(
                new LambdaQueryWrapper<PathDetail>()
                        .in(PathDetail::getPathId, pathIds)
                        .orderByAsc(PathDetail::getPathId)
                        .orderByAsc(PathDetail::getSortOrder));
        Map<Integer, List<Integer>> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        PathDetail::getPathId,
                        Collectors.mapping(PathDetail::getNodeId, Collectors.toList())
                ));
        return paths.stream()
                .map(path -> {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("pathId", path.getId());
                    item.put("pathName", path.getPathName());
                    item.put("nodeIds", grouped.getOrDefault(path.getId(), Collections.emptyList()));
                    return item;
                })
                .toList();
    }
}

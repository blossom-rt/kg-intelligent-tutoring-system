package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.CourseMapper;
import com.cupk.pojo.Course;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.service.KnowledgeNodeService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 知识点节点控制器 — 知识节点的增删改查（教师操作）
 */
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeController {

    private final KnowledgeNodeService knowledgeNodeService;
    private final CourseMapper courseMapper;

    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询知识点列表，可按课程/章节/名称筛选
     */
    @GetMapping
    public Result<List<KnowledgeNode>> list(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer chapterId,
            @RequestParam(required = false) String name) {
        // 教师角色时，未指定课程则限制为该教师负责的课程
        if (courseId == null && "teacher".equals(UserContext.getRole())) {
            List<Course> myCourses = courseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                            .eq(Course::getTeacherId, UserContext.getUserId()));
            List<Integer> ids = myCourses.stream().map(Course::getId).collect(Collectors.toList());
            if (ids.isEmpty()) return Result.success(List.of());
            List<KnowledgeNode> all = knowledgeNodeService.list(null, chapterId, name);
            all.removeIf(n -> !ids.contains(n.getCourseId()));
            return Result.success(all);
        }
        return Result.success(knowledgeNodeService.list(courseId, chapterId, name));
    }

    /**
     * 根据 ID 获取知识点详情（含课程和章节名称）
     */
    @GetMapping("/{id}")
    public Result<?> getOne(@PathVariable Integer id) {
        KnowledgeNode node = knowledgeNodeService.getById(id);
        if (node == null) return Result.error("知识点不存在");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", node.getId());
        result.put("courseId", node.getCourseId());
        result.put("courseName", node.getCourseName());
        result.put("chapterId", node.getChapterId());
        result.put("chapterName", node.getChapterName());
        result.put("name", node.getName());
        result.put("nodeName", node.getName());
        result.put("description", node.getDescription());
        result.put("nodeType", node.getNodeType());
        result.put("learningGoal", node.getLearningGoal());
        result.put("keywords", node.getKeywords());
        result.put("exampleHint", node.getExampleHint());
        result.put("difficulty", node.getDifficulty());
        result.put("expectedMinutes", node.getExpectedMinutes());
        result.put("createTime", node.getCreateTime());
        return Result.success(result);
    }

    @OperLog(module = "知识点管理", operation = "新增知识点")
    @PostMapping
    public Result<?> create(@RequestBody KnowledgeNode node) {
        checkTeacher();
        knowledgeNodeService.save(node);
        return Result.success("新增知识点成功");
    }

    @OperLog(module = "知识点管理", operation = "修改知识点")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody KnowledgeNode node) {
        checkTeacher();
        node.setId(id);
        knowledgeNodeService.update(node);
        return Result.success("修改知识点成功");
    }

    @OperLog(module = "知识点管理", operation = "删除知识点")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        knowledgeNodeService.delete(id);
        return Result.success("删除知识点成功");
    }
}

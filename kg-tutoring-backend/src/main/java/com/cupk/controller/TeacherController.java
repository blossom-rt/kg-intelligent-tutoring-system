package com.cupk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.*;
import com.cupk.pojo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final SysUserMapper userMapper;
    private final KnowledgeNodeMapper nodeMapper;
    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        long activeStudents = userMapper.selectCount(null);
        long totalNodes = nodeMapper.selectCount(null);
        long totalQuestions = questionMapper.selectCount(null);
        return ResponseEntity.ok(Map.of(
                "activeStudents", (int) activeStudents,
                "weekStudy", 0,
                "totalNodes", (int) totalNodes,
                "totalQuestions", (int) totalQuestions
        ));
    }

    @GetMapping("/courses")
    public ResponseEntity<?> listCourses() {
        return ResponseEntity.ok(courseMapper.selectList(null));
    }

    @PostMapping("/courses")
    public ResponseEntity<?> addCourse(@RequestBody Course course) {
        courseMapper.insert(course);
        return ResponseEntity.ok(Map.of("message", "课程添加成功", "id", course.getId()));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        course.setId(id);
        courseMapper.updateById(course);
        return ResponseEntity.ok(Map.of("message", "课程更新成功"));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Integer id) {
        courseMapper.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "课程已删除"));
    }

    @GetMapping("/knowledge-nodes")
    public ResponseEntity<?> listNodes(@RequestParam(required = false) Integer courseId) {
        LambdaQueryWrapper<KnowledgeNode> q = new LambdaQueryWrapper<>();
        if (courseId != null) q.eq(KnowledgeNode::getCourseId, courseId);
        return ResponseEntity.ok(nodeMapper.selectList(q));
    }

    @PostMapping("/knowledge-nodes")
    public ResponseEntity<?> addNode(@RequestBody KnowledgeNode node) {
        nodeMapper.insert(node);
        return ResponseEntity.ok(Map.of("message", "知识点添加成功", "id", node.getId()));
    }

    @PutMapping("/knowledge-nodes/{id}")
    public ResponseEntity<?> updateNode(@PathVariable Integer id, @RequestBody KnowledgeNode node) {
        node.setId(id);
        nodeMapper.updateById(node);
        return ResponseEntity.ok(Map.of("message", "知识点更新成功"));
    }

    @DeleteMapping("/knowledge-nodes/{id}")
    public ResponseEntity<?> deleteNode(@PathVariable Integer id) {
        nodeMapper.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "知识点已删除"));
    }

    @GetMapping("/questions")
    public ResponseEntity<?> listQuestions(@RequestParam(required = false) Integer nodeId) {
        LambdaQueryWrapper<Question> q = new LambdaQueryWrapper<>();
        if (nodeId != null) q.eq(Question::getNodeId, nodeId);
        return ResponseEntity.ok(questionMapper.selectList(q));
    }
}

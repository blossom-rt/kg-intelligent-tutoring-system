package com.cupk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.*;
import com.cupk.pojo.*;
import com.cupk.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final CourseMapper courseMapper;
    private final KnowledgeNodeMapper nodeMapper;
    private final StudyPathMapper pathMapper;
    private final StudyRecordMapper recordMapper;
    private final ExamRecordMapper examMapper;
    private final CrossSubjectThemeMapper themeMapper;
    private final JwtUtil jwtUtil;

    private Integer getUserId(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        Claims claims = jwtUtil.parseToken(header.substring(7));
        return Integer.valueOf(claims.getId());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpServletRequest req) {
        Integer userId = getUserId(req);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "未登录"));

        List<StudyPath> paths = pathMapper.selectList(
                new LambdaQueryWrapper<StudyPath>().eq(StudyPath::getUserId, userId).eq(StudyPath::getStatus, 0));
        List<StudyRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, userId));
        long mastered = records.stream().filter(r -> r.getMasteryLevel() != null && r.getMasteryLevel() == 2).count();
        int totalMin = records.stream().mapToInt(r -> r.getStudyMinutes() == null ? 0 : r.getStudyMinutes()).sum();
        long correctSum = records.stream().filter(r -> r.getCorrectRate() != null).count();
        double avgRate = correctSum > 0
                ? records.stream().filter(r -> r.getCorrectRate() != null).mapToDouble(r -> r.getCorrectRate().doubleValue()).average().orElse(0)
                : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("activePaths", paths.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("pathName", p.getPathName());
            m.put("progress", p.getTotalNodes() != null && p.getTotalNodes() > 0 ? 30 : 0);
            return m;
        }).toList());
        result.put("todos", List.of());
        result.put("stats", Map.of(
                "studyDays", records.isEmpty() ? 0 : 1,
                "totalMinutes", totalMin,
                "masteredNodes", (int) mastered,
                "correctRate", String.format("%.0f", avgRate * 100)
        ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses")
    public ResponseEntity<?> courses() {
        return ResponseEntity.ok(courseMapper.selectList(null));
    }

    @GetMapping("/knowledge-nodes")
    public ResponseEntity<?> knowledgeNodes(@RequestParam(required = false) Integer courseId) {
        LambdaQueryWrapper<KnowledgeNode> q = new LambdaQueryWrapper<>();
        if (courseId != null) q.eq(KnowledgeNode::getCourseId, courseId);
        return ResponseEntity.ok(nodeMapper.selectList(q));
    }

    @GetMapping("/study-paths")
    public ResponseEntity<?> studyPaths(HttpServletRequest req) {
        Integer userId = getUserId(req);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        return ResponseEntity.ok(pathMapper.selectList(
                new LambdaQueryWrapper<StudyPath>().eq(StudyPath::getUserId, userId)));
    }

    @GetMapping("/study-records")
    public ResponseEntity<?> studyRecords(HttpServletRequest req) {
        Integer userId = getUserId(req);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        return ResponseEntity.ok(recordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, userId)));
    }

    @GetMapping("/exam-records")
    public ResponseEntity<?> examRecords(HttpServletRequest req) {
        Integer userId = getUserId(req);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        return ResponseEntity.ok(examMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, userId)));
    }

    @GetMapping("/cross-subjects")
    public ResponseEntity<?> crossSubjects() {
        return ResponseEntity.ok(themeMapper.selectList(null));
    }
}

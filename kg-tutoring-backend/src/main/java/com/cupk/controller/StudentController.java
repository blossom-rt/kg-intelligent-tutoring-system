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

    private final StudyPathMapper pathMapper;
    private final PathDetailMapper detailMapper;
    private final StudyRecordMapper recordMapper;
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
                new LambdaQueryWrapper<StudyPath>().eq(StudyPath::getUserId, userId));
        List<StudyRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, userId));
        long mastered = records.stream().filter(r -> r.getMasteryLevel() != null && r.getMasteryLevel() == 2).count();
        int totalMin = records.stream().mapToInt(r -> r.getStudyMinutes() == null ? 0 : r.getStudyMinutes()).sum();
        long correctSum = records.stream().filter(r -> r.getCorrectRate() != null).count();
        double avgRate = correctSum > 0
                ? records.stream().filter(r -> r.getCorrectRate() != null).mapToDouble(r -> r.getCorrectRate().doubleValue()).average().orElse(0)
                : 0;
        int displayCorrectRate = (int) Math.round(Math.max(0, Math.min(100, avgRate)));

        Map<String, Object> result = new HashMap<>();
        result.put("isNewUser", records.isEmpty());
        result.put("activePaths", paths.stream()
            .map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("pathName", p.getPathName());
                // 从 path_detail 计算实际进度（与学习路径模块一致）
                List<PathDetail> details = detailMapper.selectList(
                    new LambdaQueryWrapper<PathDetail>().eq(PathDetail::getPathId, p.getId()));
                int total = details.size();
                long finished = details.stream().filter(d -> d.getIsFinished() != null && d.getIsFinished() == 1).count();
                int progress = total > 0 ? (int) Math.round((double) finished / total * 100) : 0;
                progress = Math.max(0, Math.min(100, progress));
                m.put("progress", progress);
                return m;
            })
            .filter(m -> (int) m.get("progress") < 100)
            .toList());
        result.put("todos", List.of());
        result.put("stats", Map.of(
                "studyDays", records.isEmpty() ? 0 : 1,
                "totalMinutes", totalMin,
                "masteredNodes", (int) mastered,
                "correctRate", displayCorrectRate
        ));
        return ResponseEntity.ok(result);
    }


    @GetMapping("/study-records")
    public ResponseEntity<?> studyRecords(HttpServletRequest req) {
        Integer userId = getUserId(req);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        return ResponseEntity.ok(recordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, userId)));
    }

}

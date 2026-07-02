package com.cupk.controller;

import com.cupk.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final SysUserMapper userMapper;
    private final KnowledgeNodeMapper nodeMapper;
    private final com.cupk.mapper.QuestionMapper questionMapper;

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
}

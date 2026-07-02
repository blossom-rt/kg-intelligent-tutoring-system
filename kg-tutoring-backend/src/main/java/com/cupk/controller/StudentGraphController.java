package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.service.KnowledgeEdgeService;
import com.cupk.service.KnowledgeNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 学生知识图谱控制器 —— 展示知识图谱的节点与边数据
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentGraphController {

    private final KnowledgeNodeService knowledgeNodeService;
    private final KnowledgeEdgeService knowledgeEdgeService;

    /**
     * 获取知识图谱数据（节点 + 边），可按课程筛选
     *
     * @param courseId 课程 ID（可选）
     */
    @GetMapping("/graph")
    public Result<Map<String, Object>> graph(@RequestParam(required = false) Integer courseId) {
        Map<String, Object> data = new HashMap<>();
        data.put("nodes", knowledgeNodeService.listByCourse(courseId));
        data.put("edges", knowledgeEdgeService.listAll());
        return Result.success(data);
    }
}

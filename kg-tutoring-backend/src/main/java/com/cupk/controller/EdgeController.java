package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.KnowledgeEdge;
import com.cupk.service.KnowledgeEdgeService;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * 知识点边（关系）控制器 —— 知识节点之间关系的管理（教师操作）
 */
@RestController
@RequestMapping("/api/edges")
@RequiredArgsConstructor
public class EdgeController {

    private final KnowledgeEdgeService knowledgeEdgeService;

    /**
     * 检查当前用户是否为教师，否则抛出无权限异常
     */
    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询边列表，可按起始节点筛选
     *
     * @param nodeId 起始节点 ID（可选）
     */
    @GetMapping
    public Result<List<KnowledgeEdge>> list(@RequestParam(required = false) Integer nodeId) {
        if (nodeId != null) {
            return Result.success(knowledgeEdgeService.listByFromNode(nodeId));
        }
        return Result.success(knowledgeEdgeService.listAll());
    }

    /**
     * 新增边关系（仅教师），自动查重
     */
    @OperLog(module = "知识图谱", operation = "新增依赖边")
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        checkTeacher();
        Integer fromNodeId = body.get("fromNodeId") != null
                ? Integer.valueOf(body.get("fromNodeId").toString())
                : (body.get("sourceId") != null ? Integer.valueOf(body.get("sourceId").toString()) : null);
        Integer toNodeId = body.get("toNodeId") != null
                ? Integer.valueOf(body.get("toNodeId").toString())
                : (body.get("targetId") != null ? Integer.valueOf(body.get("targetId").toString()) : null);
        if (fromNodeId == null || toNodeId == null) {
            return Result.error("缺少前置或后置节点ID");
        }
        if (fromNodeId.equals(toNodeId)) {
            return Result.error("前置和后置节点不能相同");
        }
        // 查重：防止重复插入
        if (knowledgeEdgeService.existsByFromNodeAndToNode(fromNodeId, toNodeId)) {
            return Result.error("该依赖关系已存在");
        }
        // 环检测：从 toNode 出发 BFS，如果能走回 fromNode 则成环
        java.util.Queue<Integer> queue = new java.util.LinkedList<>();
        java.util.Set<Integer> visited = new java.util.HashSet<>();
        queue.add(toNodeId);
        visited.add(toNodeId);
        boolean hasCycle = false;
        while (!queue.isEmpty()) {
            Integer cur = queue.poll();
            if (cur.equals(fromNodeId)) { hasCycle = true; break; }
            List<KnowledgeEdge> outEdges = knowledgeEdgeService.listByFromNode(cur);
            for (KnowledgeEdge e : outEdges) {
                if (!visited.contains(e.getToNodeId())) {
                    visited.add(e.getToNodeId());
                    queue.add(e.getToNodeId());
                }
            }
        }
        if (hasCycle) {
            return Result.error("添加该依赖边会形成环路，请检查依赖关系");
        }

        KnowledgeEdge edge = new KnowledgeEdge();
        edge.setFromNodeId(fromNodeId);
        edge.setToNodeId(toNodeId);
        knowledgeEdgeService.save(edge);
        Map<String, Object> result = new HashMap<>();
        result.put("id", edge.getId());
        result.put("fromNodeId", fromNodeId);
        result.put("toNodeId", toNodeId);
        return Result.success(result);
    }

    /**
     * 删除边关系（仅教师）
     */
    @OperLog(module = "知识图谱", operation = "删除依赖边")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        knowledgeEdgeService.delete(id);
        return Result.success("删除边关系成功");
    }
}

package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.KnowledgeEdge;
import com.cupk.service.KnowledgeEdgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
     * 新增边关系（仅教师）
     */
    @OperLog(module = "知识图谱", operation = "新增依赖边")
    @PostMapping
    public Result<?> create(@RequestBody KnowledgeEdge edge) {
        checkTeacher();
        knowledgeEdgeService.save(edge);
        return Result.success("新增边关系成功");
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

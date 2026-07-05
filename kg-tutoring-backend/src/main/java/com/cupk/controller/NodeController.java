package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.service.KnowledgeNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识点节点控制器 —— 知识节点的增删改查（教师操作）
 */
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeController {

    private final KnowledgeNodeService knowledgeNodeService;

    /**
     * 检查当前用户是否为教师，否则抛出无权限异常
     */
    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询知识点列表，可按课程筛选
     *
     * @param courseId 课程 ID（可选）
     */
    @GetMapping
    public Result<List<KnowledgeNode>> list(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String name) {
        return Result.success(knowledgeNodeService.list(courseId, name));
    }

    /**
     * 根据 ID 获取知识点详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgeNode> getOne(@PathVariable Integer id) {
        return Result.success(knowledgeNodeService.getById(id));
    }

    /**
     * 新增知识点（仅教师）
     */
    @OperLog(module = "知识点管理", operation = "新增知识点")
    @PostMapping
    public Result<?> create(@RequestBody KnowledgeNode node) {
        checkTeacher();
        knowledgeNodeService.save(node);
        return Result.success("新增知识点成功");
    }

    /**
     * 修改知识点（仅教师）
     */
    @OperLog(module = "知识点管理", operation = "修改知识点")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody KnowledgeNode node) {
        checkTeacher();
        node.setId(id);
        knowledgeNodeService.update(node);
        return Result.success("修改知识点成功");
    }

    /**
     * 删除知识点（仅教师）
     */
    @OperLog(module = "知识点管理", operation = "删除知识点")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        knowledgeNodeService.delete(id);
        return Result.success("删除知识点成功");
    }
}

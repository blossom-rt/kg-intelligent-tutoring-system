package com.cupk.controller;

import com.cupk.aspect.OperLog;
import com.cupk.common.BusinessException;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.pojo.LearningResource;
import com.cupk.service.LearningResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习资源控制器 —— 知识点配套视频和资料
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService learningResourceService;

    private void checkTeacher() {
        if (!"teacher".equals(UserContext.getRole())) {
            throw new BusinessException("无权限");
        }
    }

    /**
     * 查询知识点学习资源。学生端默认只看启用资源，教师端可传 onlyEnabled=false 查看全部。
     */
    @GetMapping("/nodes/{nodeId}/resources")
    public Result<List<LearningResource>> list(
            @PathVariable Integer nodeId,
            @RequestParam(defaultValue = "true") Boolean onlyEnabled) {
        if (!"teacher".equals(UserContext.getRole())) {
            onlyEnabled = true;
        }
        return Result.success(learningResourceService.listByNode(nodeId, onlyEnabled));
    }

    /**
     * 新增知识点学习资源（仅教师）
     */
    @OperLog(module = "学习资源管理", operation = "新增学习资源")
    @PostMapping("/nodes/{nodeId}/resources")
    public Result<?> create(@PathVariable Integer nodeId, @RequestBody LearningResource resource) {
        checkTeacher();
        resource.setNodeId(nodeId);
        learningResourceService.save(resource);
        return Result.success("新增学习资源成功");
    }

    /**
     * 修改学习资源（仅教师）
     */
    @OperLog(module = "学习资源管理", operation = "修改学习资源")
    @PutMapping("/resources/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody LearningResource resource) {
        checkTeacher();
        resource.setId(id);
        learningResourceService.update(resource);
        return Result.success("修改学习资源成功");
    }

    /**
     * 删除学习资源（仅教师）
     */
    @OperLog(module = "学习资源管理", operation = "删除学习资源")
    @DeleteMapping("/resources/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        checkTeacher();
        learningResourceService.delete(id);
        return Result.success("删除学习资源成功");
    }
}

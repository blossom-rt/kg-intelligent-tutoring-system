package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.service.StudyPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生学习路径控制器 —— 学习路径的生成与管理
 */
@RestController
@RequestMapping("/api/student/path")
@RequiredArgsConstructor
public class StudentPathController {

    private final StudyPathService studyPathService;

    /**
     * 根据目标知识点节点生成学习路径
     */
    @PostMapping("/generate")
    public Result<?> generate(@RequestBody Map<String, Object> body) {
        Integer targetNodeId = (Integer) body.get("targetNodeId");
        Integer userId = UserContext.getUserId();
        studyPathService.generatePath(userId, targetNodeId);
        return Result.success("学习路径生成成功");
    }

    /**
     * 根据跨学科主题生成学习路径
     */
    @PostMapping("/generate-by-theme")
    public Result<?> generateByTheme(@RequestBody Map<String, Object> body) {
        Integer themeId = (Integer) body.get("themeId");
        Integer userId = UserContext.getUserId();
        studyPathService.generatePathByTheme(userId, themeId);
        return Result.success("学习路径生成成功");
    }

    /**
     * 获取当前用户的所有学习路径
     */
    @GetMapping("/list")
    public Result<?> list() {
        Integer userId = UserContext.getUserId();
        return Result.success(studyPathService.listByUser(userId));
    }

    /**
     * 获取某条学习路径的详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Integer id) {
        return Result.success(studyPathService.getById(id));
    }

    /**
     * 更新路径中某个节点的完成状态
     */
    @PutMapping("/detail/{id}")
    public Result<?> updateDetailStatus(@PathVariable Integer id) {
        studyPathService.updateDetailStatus(id);
        return Result.success("更新完成状态成功");
    }

    /**
     * 删除学习路径
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        studyPathService.deletePath(id, UserContext.getUserId());
        return Result.success("删除学习路径成功");
    }
}

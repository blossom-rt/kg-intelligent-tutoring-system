package com.cupk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.PathDetailMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.PathDetail;
import com.cupk.pojo.StudyPath;
import com.cupk.service.StudyPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学生学习路径控制器 —— 学习路径的生成与管理
 */
@RestController
@RequestMapping("/api/student/path")
@RequiredArgsConstructor
public class StudentPathController {

    private final StudyPathService studyPathService;
    private final PathDetailMapper pathDetailMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;

    /**
     * 根据目标知识点节点生成学习路径
     */
    @PostMapping("/generate")
    public Result<Integer> generate(@RequestBody Map<String, Object> body) {
        Integer targetNodeId = (Integer) body.get("targetNodeId");
        Integer userId = UserContext.getUserId();
        Integer pathId = studyPathService.generatePath(userId, targetNodeId);
        return Result.success(pathId);
    }

    /**
     * 根据跨学科主题生成学习路径
     */
    @PostMapping("/generate-by-theme")
    public Result<Integer> generateByTheme(@RequestBody Map<String, Object> body) {
        Integer themeId = (Integer) body.get("themeId");
        Integer userId = UserContext.getUserId();
        Integer pathId = studyPathService.generatePathByTheme(userId, themeId);
        return Result.success(pathId);
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
    public Result<Map<String, Object>> detail(@PathVariable Integer id) {
        StudyPath path = studyPathService.getById(id);
        if (path == null) {
            return Result.error("路径不存在");
        }
        // 查询路径包含的所有详情节点（按排序顺序）
        List<PathDetail> details = pathDetailMapper.selectList(
                new LambdaQueryWrapper<PathDetail>()
                        .eq(PathDetail::getPathId, id)
                        .orderByAsc(PathDetail::getSortOrder));

        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (PathDetail d : details) {
            KnowledgeNode node = knowledgeNodeMapper.selectById(d.getNodeId());
            if (node != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("detailId", d.getId());
                item.put("nodeId", node.getId());
                item.put("id", node.getId());
                item.put("name", node.getName());
                item.put("difficulty", node.getDifficulty());
                item.put("chapter", node.getChapter());
                item.put("description", node.getDescription());
                item.put("sortOrder", d.getSortOrder());
                item.put("status", d.getIsFinished() == 1 ? "completed" : "learning");
                nodeList.add(item);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", path.getId());
        result.put("pathName", path.getPathName());
        result.put("totalNodes", path.getTotalNodes());
        result.put("totalMinutes", path.getTotalMinutes());
        result.put("aiSummary", path.getAiSummary());
        result.put("status", path.getStatus());
        result.put("nodes", nodeList);
        return Result.success(result);
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

package com.cupk.controller;

import com.cupk.common.Result;
import com.cupk.common.UserContext;
import com.cupk.mapper.UserFavoriteMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.pojo.UserFavorite;
import com.cupk.pojo.KnowledgeNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生收藏夹控制器
 */
@RestController
@RequestMapping("/api/student/favorites")
@RequiredArgsConstructor
public class StudentFavoriteController {

    private final UserFavoriteMapper userFavoriteMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;

    /** 获取当前用户的所有收藏（含知识点名称） */
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        Integer userId = UserContext.getUserId();
        List<UserFavorite> list = userFavoriteMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreateTime));

        Set<Integer> nodeIds = list.stream().map(UserFavorite::getNodeId).collect(Collectors.toSet());
        Map<Integer, KnowledgeNode> nodeMap = new HashMap<>();
        if (!nodeIds.isEmpty()) {
            List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeNode>()
                            .in(KnowledgeNode::getId, nodeIds));
            nodeMap = nodes.stream().collect(Collectors.toMap(KnowledgeNode::getId, n -> n));
        }
        final Map<Integer, KnowledgeNode> finalNodeMap = nodeMap;

        List<Map<String, Object>> result = list.stream().map(fav -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", fav.getId());
            item.put("nodeId", fav.getNodeId());
            item.put("createTime", fav.getCreateTime());
            KnowledgeNode node = finalNodeMap.get(fav.getNodeId());
            item.put("nodeName", node != null ? node.getName() : "未知");
            item.put("difficulty", node != null ? node.getDifficulty() : null);
            item.put("chapter", node != null ? node.getChapter() : null);
            return item;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /** 添加收藏 */
    @PostMapping
    public Result<?> add(@RequestBody Map<String, Object> body) {
        Integer nodeId = body.get("nodeId") != null ? Integer.valueOf(body.get("nodeId").toString()) : null;
        if (nodeId == null) return Result.error("缺少知识点ID");

        Integer userId = UserContext.getUserId();

        // 查重
        Long exist = userFavoriteMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getNodeId, nodeId));
        if (exist > 0) return Result.error("已收藏");

        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setNodeId(nodeId);
        fav.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.insert(fav);
        Map<String, Object> ret = new HashMap<>();
        ret.put("id", fav.getId());
        return Result.success(ret);
    }

    /** 取消收藏 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        userFavoriteMapper.deleteById(id);
        return Result.success("取消收藏成功");
    }
}

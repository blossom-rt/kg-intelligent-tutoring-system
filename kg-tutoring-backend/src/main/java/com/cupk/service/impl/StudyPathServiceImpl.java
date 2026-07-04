package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.CrossThemeNodeMapper;
import com.cupk.mapper.KnowledgeEdgeMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.PathDetailMapper;
import com.cupk.mapper.StudyPathMapper;
import com.cupk.pojo.CrossThemeNode;
import com.cupk.pojo.KnowledgeEdge;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.PathDetail;
import com.cupk.pojo.StudyPath;
import com.cupk.service.StudyPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 学习路径服务实现
 */
@Service
@RequiredArgsConstructor
public class StudyPathServiceImpl implements StudyPathService {

    private final StudyPathMapper studyPathMapper;
    private final PathDetailMapper pathDetailMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final KnowledgeEdgeMapper knowledgeEdgeMapper;
    private final CrossThemeNodeMapper crossThemeNodeMapper;

    @Override
    public Integer generatePath(Integer userId, Integer targetNodeId) {
        // 前置知识点集合（按拓扑顺序）
        List<KnowledgeNode> sortedNodes = topologicalSort(targetNodeId);

        // 创建学习路径主记录
        StudyPath path = new StudyPath();
        path.setUserId(userId);
        path.setTargetNodeId(targetNodeId);
        KnowledgeNode targetNode = knowledgeNodeMapper.selectById(targetNodeId);
        path.setPathName(targetNode != null ? targetNode.getName() + " 学习路径" : "学习路径");
        path.setTotalNodes(sortedNodes.size());
        int totalMinutes = sortedNodes.stream()
                .mapToInt(n -> n.getExpectedMinutes() != null ? n.getExpectedMinutes() : 0)
                .sum();
        path.setTotalMinutes(totalMinutes);
        path.setAiSummary("该路径包含 " + sortedNodes.size() + " 个知识点，预计学习时长 " + totalMinutes + " 分钟");
        path.setStatus(0);
        path.setCreateTime(LocalDateTime.now());
        studyPathMapper.insert(path);

        // 创建路径详情节点（按拓扑顺序排列）
        int order = 1;
        for (KnowledgeNode node : sortedNodes) {
            PathDetail detail = new PathDetail();
            detail.setPathId(path.getId());
            detail.setNodeId(node.getId());
            detail.setSortOrder(order++);
            detail.setIsFinished(0);
            pathDetailMapper.insert(detail);
        }
        return path.getId();
    }

    @Override
    public Integer generatePathByTheme(Integer userId, Integer themeId) {
        // 查询主题关联的所有知识点
        List<CrossThemeNode> mappings = crossThemeNodeMapper.selectList(
                new LambdaQueryWrapper<CrossThemeNode>()
                        .eq(CrossThemeNode::getThemeId, themeId));

        if (mappings.isEmpty()) {
            return null;
        }

        List<Integer> nodeIds = new ArrayList<>();
        for (CrossThemeNode m : mappings) {
            nodeIds.add(m.getNodeId());
        }
        List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(
                new LambdaQueryWrapper<KnowledgeNode>().in(KnowledgeNode::getId, nodeIds));

        // 创建学习路径主记录
        StudyPath path = new StudyPath();
        path.setUserId(userId);
        path.setTargetNodeId(nodeIds.get(nodeIds.size() - 1));
        path.setPathName("跨学科主题学习路径");
        path.setTotalNodes(nodes.size());
        int totalMinutes = nodes.stream()
                .mapToInt(n -> n.getExpectedMinutes() != null ? n.getExpectedMinutes() : 0)
                .sum();
        path.setTotalMinutes(totalMinutes);
        path.setAiSummary("跨学科主题路径，包含 " + nodes.size() + " 个知识点");
        path.setStatus(0);
        path.setCreateTime(LocalDateTime.now());
        studyPathMapper.insert(path);

        // 创建路径详情
        int order = 1;
        for (KnowledgeNode node : nodes) {
            PathDetail detail = new PathDetail();
            detail.setPathId(path.getId());
            detail.setNodeId(node.getId());
            detail.setSortOrder(order++);
            detail.setIsFinished(0);
            pathDetailMapper.insert(detail);
        }
        return path.getId();
    }

    @Override
    public List<StudyPath> listByUser(Integer userId) {
        return studyPathMapper.selectList(
                new LambdaQueryWrapper<StudyPath>()
                        .eq(StudyPath::getUserId, userId)
                        .orderByDesc(StudyPath::getCreateTime));
    }

    @Override
    public StudyPath getById(Integer id) {
        return studyPathMapper.selectById(id);
    }

    @Override
    public void updateDetailStatus(Integer detailId) {
        PathDetail detail = pathDetailMapper.selectById(detailId);
        if (detail != null) {
            detail.setIsFinished(1);
            pathDetailMapper.updateById(detail);

            // 检查是否所有节点都已完成，更新路径主表状态
            Long unfinished = pathDetailMapper.selectCount(
                    new LambdaQueryWrapper<PathDetail>()
                            .eq(PathDetail::getPathId, detail.getPathId())
                            .eq(PathDetail::getIsFinished, 0));
            if (unfinished == 0) {
                StudyPath path = studyPathMapper.selectById(detail.getPathId());
                if (path != null) {
                    path.setStatus(1);
                    studyPathMapper.updateById(path);
                }
            }
        }
    }

    @Override
    public void deletePath(Integer pathId, Integer userId) {
        // 先删路径详情（子表），再删路径（父表）
        pathDetailMapper.delete(
                new LambdaQueryWrapper<PathDetail>()
                        .eq(PathDetail::getPathId, pathId));
        // 只允许删除自己的路径
        studyPathMapper.delete(
                new LambdaQueryWrapper<StudyPath>()
                        .eq(StudyPath::getId, pathId)
                        .eq(StudyPath::getUserId, userId));
    }

    /**
     * 拓扑排序：从目标节点反向BFS找到所有前置依赖，按拓扑顺序排列
     */
    private List<KnowledgeNode> topologicalSort(Integer targetNodeId) {
        // 反向 BFS：找到所有前置节点
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(targetNodeId);
        visited.add(targetNodeId);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            // 找指向当前节点的所有边（前置节点）
            List<KnowledgeEdge> incomingEdges = knowledgeEdgeMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeEdge>()
                            .eq(KnowledgeEdge::getToNodeId, current));
            for (KnowledgeEdge edge : incomingEdges) {
                if (!visited.contains(edge.getFromNodeId())) {
                    visited.add(edge.getFromNodeId());
                    queue.add(edge.getFromNodeId());
                }
            }
        }

        // 批量查询节点
        List<KnowledgeNode> allNodes = knowledgeNodeMapper.selectList(
                new LambdaQueryWrapper<KnowledgeNode>().in(KnowledgeNode::getId, visited));

        // 拓扑排序（Kahn 算法）
        Map<Integer, KnowledgeNode> nodeMap = new HashMap<>();
        Map<Integer, Integer> inDegree = new HashMap<>();
        Map<Integer, List<Integer>> adj = new HashMap<>();

        for (KnowledgeNode node : allNodes) {
            nodeMap.put(node.getId(), node);
            inDegree.put(node.getId(), 0);
            adj.put(node.getId(), new ArrayList<>());
        }

        // 注意：只考虑已访问节点集合内的边
        for (KnowledgeNode node : allNodes) {
            List<KnowledgeEdge> outEdges = knowledgeEdgeMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeEdge>()
                            .eq(KnowledgeEdge::getFromNodeId, node.getId()));
            for (KnowledgeEdge edge : outEdges) {
                if (visited.contains(edge.getToNodeId())) {
                    adj.get(node.getId()).add(edge.getToNodeId());
                    inDegree.merge(edge.getToNodeId(), 1, Integer::sum);
                }
            }
        }

        Queue<Integer> zeroQueue = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                zeroQueue.add(entry.getKey());
            }
        }

        List<KnowledgeNode> sorted = new ArrayList<>();
        while (!zeroQueue.isEmpty()) {
            Integer nodeId = zeroQueue.poll();
            sorted.add(nodeMap.get(nodeId));
            for (Integer neighbor : adj.get(nodeId)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    zeroQueue.add(neighbor);
                }
            }
        }

        return sorted;
    }
}

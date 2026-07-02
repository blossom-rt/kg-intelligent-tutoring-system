package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.KnowledgeEdgeMapper;
import com.cupk.pojo.KnowledgeEdge;
import com.cupk.service.KnowledgeEdgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识点依赖边服务实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgeEdgeServiceImpl implements KnowledgeEdgeService {

    private final KnowledgeEdgeMapper knowledgeEdgeMapper;

    @Override
    public List<KnowledgeEdge> listByFromNode(Integer nodeId) {
        return knowledgeEdgeMapper.selectList(
                new LambdaQueryWrapper<KnowledgeEdge>()
                        .eq(KnowledgeEdge::getFromNodeId, nodeId));
    }

    @Override
    public List<KnowledgeEdge> listAll() {
        return knowledgeEdgeMapper.selectList(null);
    }

    @Override
    public void save(KnowledgeEdge edge) {
        knowledgeEdgeMapper.insert(edge);
    }

    @Override
    public void delete(Integer id) {
        knowledgeEdgeMapper.deleteById(id);
    }
}

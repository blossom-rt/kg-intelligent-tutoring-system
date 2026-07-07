package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.service.KnowledgeNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识点节点服务实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgeNodeServiceImpl implements KnowledgeNodeService {

    private final KnowledgeNodeMapper knowledgeNodeMapper;

    @Override
    public List<KnowledgeNode> list(Integer courseId, String name) {
        LambdaQueryWrapper<KnowledgeNode> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(KnowledgeNode::getCourseId, courseId);
        }
        if (name != null && !name.isBlank()) {
            wrapper.like(KnowledgeNode::getName, name);
        }
        wrapper.orderByDesc(KnowledgeNode::getCreateTime);
        return knowledgeNodeMapper.selectList(wrapper);
    }

    @Override
    public KnowledgeNode getById(Integer id) {
        return knowledgeNodeMapper.selectById(id);
    }

    @Override
    public void save(KnowledgeNode node) {
        knowledgeNodeMapper.insert(node);
    }

    @Override
    public void update(KnowledgeNode node) {
        knowledgeNodeMapper.updateById(node);
    }

    @Override
    public void delete(Integer id) {
        knowledgeNodeMapper.deleteById(id);
    }

    @Override
    public List<KnowledgeNode> listByCourse(Integer courseId) {
        LambdaQueryWrapper<KnowledgeNode> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(KnowledgeNode::getCourseId, courseId);
        }
        return knowledgeNodeMapper.selectList(wrapper.orderByAsc(KnowledgeNode::getChapter));
    }
}

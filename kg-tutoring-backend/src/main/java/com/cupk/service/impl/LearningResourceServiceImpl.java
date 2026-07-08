package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.LearningResourceMapper;
import com.cupk.pojo.LearningResource;
import com.cupk.service.LearningResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学习资源服务实现
 */
@Service
@RequiredArgsConstructor
public class LearningResourceServiceImpl implements LearningResourceService {

    private final LearningResourceMapper learningResourceMapper;

    @Override
    public List<LearningResource> listByNode(Integer nodeId, Boolean onlyEnabled) {
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningResource::getNodeId, nodeId);
        if (Boolean.TRUE.equals(onlyEnabled)) {
            wrapper.eq(LearningResource::getStatus, 1);
        }
        wrapper.orderByAsc(LearningResource::getSortOrder)
                .orderByDesc(LearningResource::getCreateTime);
        return learningResourceMapper.selectList(wrapper);
    }

    @Override
    public void save(LearningResource resource) {
        if (resource.getResourceType() == null || resource.getResourceType().isBlank()) {
            resource.setResourceType("video");
        }
        if (resource.getSource() == null || resource.getSource().isBlank()) {
            resource.setSource("custom");
        }
        if (resource.getSortOrder() == null) {
            resource.setSortOrder(0);
        }
        if (resource.getStatus() == null) {
            resource.setStatus(1);
        }
        learningResourceMapper.insert(resource);
    }

    @Override
    public void update(LearningResource resource) {
        learningResourceMapper.updateById(resource);
    }

    @Override
    public void delete(Integer id) {
        learningResourceMapper.deleteById(id);
    }
}

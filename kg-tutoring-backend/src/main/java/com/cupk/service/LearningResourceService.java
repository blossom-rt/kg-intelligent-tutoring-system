package com.cupk.service;

import com.cupk.pojo.LearningResource;

import java.util.List;

/**
 * 学习资源服务
 */
public interface LearningResourceService {

    /** 查询某知识点下的学习资源 */
    List<LearningResource> listByNode(Integer nodeId, Boolean onlyEnabled);

    /** 新增学习资源 */
    void save(LearningResource resource);

    /** 更新学习资源 */
    void update(LearningResource resource);

    /** 删除学习资源 */
    void delete(Integer id);
}

package com.cupk.service;

import com.cupk.pojo.KnowledgeNode;

import java.util.List;

/**
 * 知识点节点服务
 */
public interface KnowledgeNodeService {

    /** 查询知识点列表（可按课程过滤） */
    List<KnowledgeNode> list(Integer courseId);

    /** 根据ID查询知识点 */
    KnowledgeNode getById(Integer id);

    /** 新增知识点 */
    void save(KnowledgeNode node);

    /** 更新知识点 */
    void update(KnowledgeNode node);

    /** 删除知识点 */
    void delete(Integer id);

    /** 查询某课程下的所有知识点 */
    List<KnowledgeNode> listByCourse(Integer courseId);
}

package com.cupk.service;

import com.cupk.pojo.KnowledgeEdge;

import java.util.List;

/**
 * 知识点依赖边服务
 */
public interface KnowledgeEdgeService {

    /** 查询某节点的所有出边 */
    List<KnowledgeEdge> listByFromNode(Integer nodeId);

    /** 查询某节点的所有入边（指向该节点的边） */
    List<KnowledgeEdge> listByToNode(Integer toNodeId);

    /** 查询所有边 */
    List<KnowledgeEdge> listAll();

    /** 新增边 */
    void save(KnowledgeEdge edge);

    /** 检查边是否已存在（fromNodeId → toNodeId） */
    boolean existsByFromNodeAndToNode(Integer fromNodeId, Integer toNodeId);

    /** 删除边 */
    void delete(Integer id);
}

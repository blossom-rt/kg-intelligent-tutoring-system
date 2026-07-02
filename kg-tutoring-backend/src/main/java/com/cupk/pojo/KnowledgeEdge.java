package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 知识点依赖边
 */
@Data
@TableName("knowledge_edge")
public class KnowledgeEdge {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 前置知识点ID */
    private Integer fromNodeId;

    /** 后继知识点ID */
    private Integer toNodeId;

    /** 关系类型 */
    private String relationType;

    /** 是否跨学科：0否 1是 */
    private Integer isCrossSubject;
}

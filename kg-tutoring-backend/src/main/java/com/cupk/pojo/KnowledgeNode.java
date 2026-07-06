package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点节点
 */
@Data
@TableName("knowledge_node")
public class KnowledgeNode {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属课程ID */
    private Integer courseId;

    /** 知识点名称 */
    private String name;

    /** 知识点讲解内容 */
    private String description;

    /** 节点类型：concept概念 skill技能 application应用 */
    private String nodeType;

    /** 学习目标 */
    private String learningGoal;

    /** 关键词，逗号分隔 */
    private String keywords;

    /** 例题或学习提示 */
    private String exampleHint;

    /** 难度：1基础 2进阶 3困难 */
    private Integer difficulty;

    /** 所属章节 */
    private String chapter;

    /** 预计学习时长(分钟) */
    private Integer expectedMinutes;

    /** 创建时间 */
    private LocalDateTime createTime;
}

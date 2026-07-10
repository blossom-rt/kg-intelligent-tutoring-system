package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 习题
 */
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 关联知识点ID */
    private Integer nodeId;

    /** 题干 */
    private String content;

    /** 选项(JSON格式) */
    private String options;

    /** 标准答案 */
    private String answer;

    /** 题目解析 */
    private String analysis;

    /** 难度等级 */
    private Integer difficulty;

    /** 题型：single/multi/judge */
    private String questionType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 知识点名称（联表查询填充） */
    @TableField(exist = false)
    private String nodeName;
}

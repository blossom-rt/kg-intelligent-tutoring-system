package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习路径主表
 */
@Data
@TableName("study_path")
public class StudyPath {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 学生ID */
    private Integer userId;

    /** 目标知识点ID */
    private Integer targetNodeId;

    /** 路径名称 */
    private String pathName;

    /** 总节点数 */
    private Integer totalNodes;

    /** 总预计时长(分钟) */
    private Integer totalMinutes;

    /** AI路径总览 */
    private String aiSummary;

    /** 状态：0进行中 1已完成 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}

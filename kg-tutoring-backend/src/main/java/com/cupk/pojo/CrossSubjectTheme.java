package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跨学科主题
 */
@Data
@TableName("cross_subject_theme")
public class CrossSubjectTheme {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 主题名称 */
    private String themeName;

    /** 主题背景与目标 */
    private String description;

    /** 主题难度 */
    private Integer difficulty;

    /** 发布教师ID */
    private Integer publisherId;

    /** 状态：0下架 1发布 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}

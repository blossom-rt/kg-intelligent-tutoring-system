package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识点学习记录
 */
@Data
@TableName("study_record")
public class StudyRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 学生ID */
    private Integer userId;

    /** 知识点ID */
    private Integer nodeId;

    /** 掌握度：0未学 1学习中 2已掌握 */
    private Integer masteryLevel;

    /** 答题正确率 */
    private BigDecimal correctRate;

    /** 累计学习时长(分钟) */
    private Integer studyMinutes;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

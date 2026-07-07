package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测评记录
 */
@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 学生ID */
    private Integer userId;

    /** 测评ID */
    private Integer examId;

    /** 所属课程ID */
    private Integer courseId;

    /** 试卷总分 */
    private Integer totalScore;

    /** 学生得分 */
    private BigDecimal userScore;

    /** AI诊断报告 */
    private String aiReport;

    /** 完成时间 */
    private LocalDateTime createTime;
}

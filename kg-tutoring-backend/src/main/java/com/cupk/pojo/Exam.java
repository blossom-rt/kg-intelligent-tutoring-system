package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测评定义
 */
@Data
@TableName("exam")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 测评名称 */
    private String examName;

    /** 所属课程ID */
    private Integer courseId;

    /** 发布教师ID */
    private Integer creatorId;

    /** 试卷总分 */
    private Integer totalScore;

    /** 状态：0草稿 1发布 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}

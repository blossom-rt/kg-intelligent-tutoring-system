package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 测评题目关联
 */
@Data
@TableName("exam_question")
public class ExamQuestion {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 测评ID */
    private Integer examId;

    /** 题目ID */
    private Integer questionId;

    /** 题目顺序 */
    private Integer sortOrder;
}

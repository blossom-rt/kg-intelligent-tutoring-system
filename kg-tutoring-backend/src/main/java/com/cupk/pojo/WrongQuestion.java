package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本
 */
@Data
@TableName("wrong_question")
public class WrongQuestion {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 学生ID */
    private Integer userId;

    /** 题目ID */
    private Integer questionId;

    /** 错误答案 */
    private String wrongAnswer;

    /** 错误次数 */
    private Integer wrongCount;

    /** AI错题讲解 */
    private String aiExplain;

    /** 首次错误时间 */
    private LocalDateTime createTime;
}

package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属学科 */
    private String subject;

    /** 课程名称 */
    private String courseName;

    /** 课程简介 */
    private String description;

    /** 负责教师ID */
    private Integer teacherId;

    /** 教师姓名（联表查询填充） */
    @TableField(exist = false)
    private String teacherName;

    /** 创建时间 */
    private LocalDateTime createTime;
}

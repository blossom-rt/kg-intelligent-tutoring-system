package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节
 */
@Data
@TableName("chapter")
public class Chapter {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属课程ID */
    private Integer courseId;

    /** 章节名称 */
    private String chapterName;

    /** 章节排序号 */
    private Integer sort;

    /** 章节教学目标 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;
}

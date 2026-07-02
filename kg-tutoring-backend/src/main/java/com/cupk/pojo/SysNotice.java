package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告
 */
@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 公告标题 */
    private String title;

    /** 公告正文 */
    private String content;

    /** 发布人ID */
    private Integer publisherId;

    /** 推送对象：all/student/teacher */
    private String targetRole;

    /** 发布时间 */
    private LocalDateTime createTime;
}

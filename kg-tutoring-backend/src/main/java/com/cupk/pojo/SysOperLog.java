package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 操作人ID */
    private Integer userId;

    /** 操作模块 */
    private String module;

    /** 操作内容 */
    private String operation;

    /** 操作IP */
    private String ip;

    /** 操作时间 */
    private LocalDateTime createTime;
}

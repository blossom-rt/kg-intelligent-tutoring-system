package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI调用日志
 */
@Data
@TableName("ai_call_log")
public class AiCallLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 调用用户ID */
    private Integer userId;

    /** 调用场景 */
    private String scene;

    /** 输入提示词 */
    private String prompt;

    /** AI返回结果 */
    private String result;

    /** 调用耗时(ms) */
    private Integer callDuration;

    /** 状态：1成功 0失败 */
    private Integer status;

    /** 调用时间 */
    private LocalDateTime createTime;
}

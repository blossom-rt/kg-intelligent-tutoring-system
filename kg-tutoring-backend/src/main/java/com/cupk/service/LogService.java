package com.cupk.service;

import com.cupk.pojo.AiCallLog;
import com.cupk.pojo.SysOperLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志查询服务（只读）
 */
public interface LogService {

    /** 查询操作日志列表（可按模块、时间范围过滤） */
    List<SysOperLog> listOperLog(String module, LocalDateTime startDate, LocalDateTime endDate);

    /** 查询AI调用日志列表（可按场景过滤） */
    List<AiCallLog> listAiLog(String scene);
}

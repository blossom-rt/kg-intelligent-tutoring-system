package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.AiCallLogMapper;
import com.cupk.mapper.SysOperLogMapper;
import com.cupk.pojo.AiCallLog;
import com.cupk.pojo.SysOperLog;
import com.cupk.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志查询服务实现（只读）
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final SysOperLogMapper sysOperLogMapper;
    private final AiCallLogMapper aiCallLogMapper;

    @Override
    public List<SysOperLog> listOperLog(String module, LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.eq(SysOperLog::getModule, module);
        }
        if (startDate != null) {
            wrapper.ge(SysOperLog::getCreateTime, startDate);
        }
        if (endDate != null) {
            wrapper.le(SysOperLog::getCreateTime, endDate);
        }
        wrapper.orderByDesc(SysOperLog::getCreateTime);
        return sysOperLogMapper.selectList(wrapper);
    }

    @Override
    public List<AiCallLog> listAiLog(String scene) {
        LambdaQueryWrapper<AiCallLog> wrapper = new LambdaQueryWrapper<>();
        if (scene != null && !scene.isEmpty()) {
            wrapper.eq(AiCallLog::getScene, scene);
        }
        wrapper.orderByDesc(AiCallLog::getCreateTime);
        return aiCallLogMapper.selectList(wrapper);
    }
}

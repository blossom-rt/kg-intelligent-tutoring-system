package com.cupk.aspect;

import com.cupk.common.UserContext;
import com.cupk.mapper.SysOperLogMapper;
import com.cupk.pojo.SysOperLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志切面 —— 拦截 @OperLog 注解的方法，自动写入 sys_oper_log 表
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogMapper sysOperLogMapper;
    private final HttpServletRequest request;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        // 执行目标方法
        Object result = joinPoint.proceed();

        try {
            SysOperLog logEntry = new SysOperLog();
            Integer uid = UserContext.getUserId();
            if (uid != null) logEntry.setUserId(uid);
            logEntry.setModule(operLog.module());
            logEntry.setOperation(operLog.operation());

            // 获取客户端 IP
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 处理多级代理场景，取第一个非 unknown 的 IP
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            logEntry.setIp(ip);
            logEntry.setCreateTime(LocalDateTime.now());

            sysOperLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("操作日志记录失败", e);
        }

        return result;
    }
}

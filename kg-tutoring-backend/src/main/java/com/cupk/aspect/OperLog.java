package com.cupk.aspect;

import java.lang.annotation.*;

/**
 * 操作日志注解 —— 标注在需要自动记录操作日志的方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 操作模块 */
    String module() default "";

    /** 操作描述 */
    String operation() default "";
}

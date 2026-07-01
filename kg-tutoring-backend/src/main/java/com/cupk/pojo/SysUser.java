package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String realName;

    private String email;

    private String password;

    private Integer roleId;

    private Integer status;

    private LocalDateTime createTime;

    /** 关联角色（非数据库字段，联表查询用） */
    @TableField(exist = false)
    private SysRole role;
}

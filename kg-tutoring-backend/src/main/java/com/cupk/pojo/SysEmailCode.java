package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_email_code")
public class SysEmailCode {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String email;
    private String code;
    private String type;
    private LocalDateTime expireTime;
    private Integer isUsed;
    private LocalDateTime createTime;
}

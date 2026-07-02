package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cross_subject_theme")
public class CrossSubjectTheme {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String themeName;
    private String description;
    private Integer difficulty;
    private Integer publisherId;
    private Integer status;
    private LocalDateTime createTime;
}

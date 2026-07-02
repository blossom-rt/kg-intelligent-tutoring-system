package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("study_path")
public class StudyPath {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer targetNodeId;
    private String pathName;
    private Integer totalNodes;
    private Integer totalMinutes;
    private String aiSummary;
    private Integer status;
    private LocalDateTime createTime;
}

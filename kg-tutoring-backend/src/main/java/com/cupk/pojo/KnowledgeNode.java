package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_node")
public class KnowledgeNode {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer courseId;
    private String name;
    private String description;
    private Integer difficulty;
    private String chapter;
    private Integer expectedMinutes;
    private LocalDateTime createTime;
}

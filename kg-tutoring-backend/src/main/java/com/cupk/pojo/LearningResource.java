package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点学习资源
 */
@Data
@TableName("learning_resource")
public class LearningResource {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 关联知识点ID */
    private Integer nodeId;

    /** 资源标题 */
    private String title;

    /** 资源类型：video/article/pdf/link */
    private String resourceType;

    /** 资源地址 */
    private String url;

    /** 封面地址 */
    private String coverUrl;

    /** 视频或资源时长(秒) */
    private Integer durationSeconds;

    /** 资源来源：bilibili/mooc/local/custom */
    private String source;

    /** 排序值 */
    private Integer sortOrder;

    /** 状态：1启用 0停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

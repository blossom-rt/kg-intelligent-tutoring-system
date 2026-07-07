package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学习路径详情
 */
@Data
@TableName("path_detail")
public class PathDetail {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属路径ID */
    private Integer pathId;

    /** 知识点ID */
    private Integer nodeId;

    /** 学习顺序号 */
    private Integer sortOrder;

    /** 是否完成：0否 1是 */
    private Integer isFinished;
}

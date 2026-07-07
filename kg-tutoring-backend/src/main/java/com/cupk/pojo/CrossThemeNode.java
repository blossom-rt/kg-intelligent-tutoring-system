package com.cupk.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 主题-知识点关联
 */
@Data
@TableName("cross_theme_node")
public class CrossThemeNode {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 主题ID */
    private Integer themeId;

    /** 知识点ID */
    private Integer nodeId;
}

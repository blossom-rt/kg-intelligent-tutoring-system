package com.cupk.service;

import com.cupk.pojo.StudyPath;

import java.util.List;

/**
 * 学习路径服务
 */
public interface StudyPathService {

    /** 根据目标知识点生成学习路径，返回生成的路径ID */
    Integer generatePath(Integer userId, Integer targetNodeId);

    /** 根据跨学科主题生成学习路径，返回生成的路径ID */
    Integer generatePathByTheme(Integer userId, Integer themeId);

    /** 查询某用户的所有学习路径 */
    List<StudyPath> listByUser(Integer userId);

    /** 根据ID查询学习路径 */
    StudyPath getById(Integer id);

    /** 更新路径详情节点的完成状态 */
    void updateDetailStatus(Integer detailId);

    /** 删除学习路径 */
    void deletePath(Integer pathId, Integer userId);
}

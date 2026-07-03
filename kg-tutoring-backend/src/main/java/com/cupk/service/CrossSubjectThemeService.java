package com.cupk.service;

import com.cupk.pojo.CrossSubjectTheme;

import java.util.List;

/**
 * 跨学科主题服务
 */
public interface CrossSubjectThemeService {

    /** 查询跨学科主题列表 */
    List<CrossSubjectTheme> list(Integer difficulty, Integer status);

    /** 根据ID查询主题 */
    CrossSubjectTheme getById(Integer id);

    /** 新增主题 */
    void save(CrossSubjectTheme theme);

    /** 更新主题 */
    void update(CrossSubjectTheme theme);

    /** 删除主题 */
    void delete(Integer id);

    /** 切换主题上下架状态 */
    void toggleStatus(Integer id);
}

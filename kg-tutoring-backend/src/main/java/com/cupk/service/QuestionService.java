package com.cupk.service;

import com.cupk.pojo.Question;

import java.util.List;

/**
 * 习题服务
 */
public interface QuestionService {

    /** 查询习题列表（可按知识点、学科、难度过滤） */
    List<Question> list(Integer nodeId, Integer courseId, Integer difficulty);

    /** 根据ID查询习题 */
    Question getById(Integer id);

    /** 新增习题 */
    void save(Question q);

    /** 更新习题 */
    void update(Question q);

    /** 删除习题 */
    void delete(Integer id);

    /** 查询某知识点下的所有习题 */
    List<Question> listByNode(Integer nodeId);
}

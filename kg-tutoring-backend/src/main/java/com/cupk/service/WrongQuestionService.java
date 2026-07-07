package com.cupk.service;

import com.cupk.pojo.WrongQuestion;

import java.util.List;

/**
 * 错题本服务
 */
public interface WrongQuestionService {

    /** 查询某用户的错题列表 */
    List<WrongQuestion> listByUser(Integer userId);

    /** 移除错题 */
    void remove(Integer id, Integer userId);
}

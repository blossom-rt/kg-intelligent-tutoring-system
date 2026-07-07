package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.WrongQuestion;
import com.cupk.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 错题本服务实现
 */
@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;

    @Override
    public List<WrongQuestion> listByUser(Integer userId) {
        return wrongQuestionMapper.selectList(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getUserId, userId)
                        .orderByDesc(WrongQuestion::getCreateTime));
    }

    @Override
    public void remove(Integer id, Integer userId) {
        wrongQuestionMapper.delete(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getId, id)
                        .eq(WrongQuestion::getUserId, userId));
    }
}

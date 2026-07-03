package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.pojo.Question;
import com.cupk.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 习题服务实现
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    @Override
    public List<Question> list(Integer nodeId, Integer courseId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (nodeId != null) {
            wrapper.eq(Question::getNodeId, nodeId);
        }
        if (courseId != null) {
            wrapper.inSql(Question::getNodeId, "SELECT id FROM knowledge_node WHERE course_id = " + courseId);
        }
        wrapper.orderByDesc(Question::getCreateTime);
        return questionMapper.selectList(wrapper);
    }

    @Override
    public Question getById(Integer id) {
        return questionMapper.selectById(id);
    }

    @Override
    public void save(Question q) {
        questionMapper.insert(q);
    }

    @Override
    public void update(Question q) {
        questionMapper.updateById(q);
    }

    @Override
    public void delete(Integer id) {
        questionMapper.deleteById(id);
    }

    @Override
    public List<Question> listByNode(Integer nodeId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getNodeId, nodeId)
                        .orderByAsc(Question::getDifficulty));
    }
}

package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.CrossSubjectThemeMapper;
import com.cupk.mapper.CrossThemeNodeMapper;
import com.cupk.pojo.CrossSubjectTheme;
import com.cupk.pojo.CrossThemeNode;
import com.cupk.service.CrossSubjectThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 跨学科主题服务实现
 */
@Service
@RequiredArgsConstructor
public class CrossSubjectThemeServiceImpl implements CrossSubjectThemeService {

    private final CrossSubjectThemeMapper crossSubjectThemeMapper;
    private final CrossThemeNodeMapper crossThemeNodeMapper;

    @Override
    public List<CrossSubjectTheme> list(Integer difficulty, Integer status) {
        LambdaQueryWrapper<CrossSubjectTheme> wrapper = new LambdaQueryWrapper<CrossSubjectTheme>()
                .orderByDesc(CrossSubjectTheme::getCreateTime);
        if (difficulty != null) {
            wrapper.eq(CrossSubjectTheme::getDifficulty, difficulty);
        }
        if (status != null) {
            wrapper.eq(CrossSubjectTheme::getStatus, status);
        }
        return crossSubjectThemeMapper.selectList(wrapper);
    }

    @Override
    public CrossSubjectTheme getById(Integer id) {
        return crossSubjectThemeMapper.selectById(id);
    }

    @Override
    public void save(CrossSubjectTheme theme) {
        crossSubjectThemeMapper.insert(theme);
    }

    @Override
    public void update(CrossSubjectTheme theme) {
        crossSubjectThemeMapper.updateById(theme);
    }

    @Override
    public void delete(Integer id) {
        // 级联删除关联的知识点映射
        crossThemeNodeMapper.delete(
                new LambdaQueryWrapper<CrossThemeNode>()
                        .eq(CrossThemeNode::getThemeId, id));
        crossSubjectThemeMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Integer id) {
        CrossSubjectTheme theme = crossSubjectThemeMapper.selectById(id);
        if (theme != null) {
            // 0->1 下架变发布，1->0 发布变下架
            theme.setStatus(theme.getStatus() == 1 ? 0 : 1);
            crossSubjectThemeMapper.updateById(theme);
        }
    }
}

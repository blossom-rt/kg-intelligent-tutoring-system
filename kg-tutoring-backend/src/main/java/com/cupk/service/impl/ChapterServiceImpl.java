package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.ChapterMapper;
import com.cupk.pojo.Chapter;
import com.cupk.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterMapper chapterMapper;

    @Override
    public List<Chapter> listByCourse(Integer courseId) {
        return chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getCourseId, courseId)
                        .orderByAsc(Chapter::getSort));
    }

    @Override
    public Chapter getById(Integer id) {
        return chapterMapper.selectById(id);
    }

    @Override
    public void save(Chapter chapter) {
        chapterMapper.insert(chapter);
    }

    @Override
    public void update(Chapter chapter) {
        chapterMapper.updateById(chapter);
    }

    @Override
    public void delete(Integer id) {
        chapterMapper.deleteById(id);
    }

    @Override
    public void updateSort(Integer id, Integer sort) {
        Chapter chapter = chapterMapper.selectById(id);
        if (chapter != null) {
            chapter.setSort(sort);
            chapterMapper.updateById(chapter);
        }
    }
}

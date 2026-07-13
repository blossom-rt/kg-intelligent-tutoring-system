package com.cupk.service;

import com.cupk.pojo.Chapter;

import java.util.List;

public interface ChapterService {

    List<Chapter> listByCourse(Integer courseId);

    Chapter getById(Integer id);

    void save(Chapter chapter);

    void update(Chapter chapter);

    void delete(Integer id);

    void updateSort(Integer id, Integer sort);
}

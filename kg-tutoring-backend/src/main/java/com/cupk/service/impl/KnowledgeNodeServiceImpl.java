package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.ChapterMapper;
import com.cupk.mapper.CourseMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.pojo.Chapter;
import com.cupk.pojo.Course;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.service.KnowledgeNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识点节点服务实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgeNodeServiceImpl implements KnowledgeNodeService {

    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final ChapterMapper chapterMapper;
    private final CourseMapper courseMapper;

    @Override
    public List<KnowledgeNode> list(Integer courseId, Integer chapterId, String name) {
        LambdaQueryWrapper<KnowledgeNode> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(KnowledgeNode::getCourseId, courseId);
        }
        if (chapterId != null) {
            wrapper.eq(KnowledgeNode::getChapterId, chapterId);
        }
        if (name != null && !name.isBlank()) {
            wrapper.like(KnowledgeNode::getName, name);
        }
        wrapper.orderByDesc(KnowledgeNode::getCreateTime);
        List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(wrapper);
        enrichNodeNames(nodes);
        return nodes;
    }

    @Override
    public KnowledgeNode getById(Integer id) {
        KnowledgeNode node = knowledgeNodeMapper.selectById(id);
        if (node != null) enrichSingleNode(node);
        return node;
    }

    @Override
    public void save(KnowledgeNode node) {
        knowledgeNodeMapper.insert(node);
    }

    @Override
    public void update(KnowledgeNode node) {
        knowledgeNodeMapper.updateById(node);
    }

    @Override
    public void delete(Integer id) {
        knowledgeNodeMapper.deleteById(id);
    }

    @Override
    public List<KnowledgeNode> listByCourse(Integer courseId) {
        LambdaQueryWrapper<KnowledgeNode> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(KnowledgeNode::getCourseId, courseId);
        }
        List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(wrapper.orderByAsc(KnowledgeNode::getChapterId));
        enrichNodeNames(nodes);
        return nodes;
    }

    @Override
    public List<KnowledgeNode> listByIds(List<Integer> ids) {
        List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(
                new LambdaQueryWrapper<KnowledgeNode>()
                        .in(KnowledgeNode::getId, ids));
        enrichNodeNames(nodes);
        return nodes;
    }

    private void enrichNodeNames(List<KnowledgeNode> nodes) {
        if (nodes.isEmpty()) return;
        // 收集所有 chapterId
        var chapterIds = nodes.stream()
                .map(KnowledgeNode::getChapterId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        var courseIds = nodes.stream()
                .map(KnowledgeNode::getCourseId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Integer, String> chapterMap = new HashMap<>();
        Map<Integer, String> courseMap = new HashMap<>();

        if (!chapterIds.isEmpty()) {
            List<Chapter> chapters = chapterMapper.selectList(
                    new LambdaQueryWrapper<Chapter>().in(Chapter::getId, chapterIds));
            chapterMap = chapters.stream()
                    .collect(Collectors.toMap(Chapter::getId, Chapter::getChapterName));
        }
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>().in(Course::getId, courseIds));
            courseMap = courses.stream()
                    .collect(Collectors.toMap(Course::getId, Course::getCourseName));
        }

        final var finalChapterMap = chapterMap;
        final var finalCourseMap = courseMap;
        nodes.forEach(n -> {
            if (n.getChapterId() != null) n.setChapterName(finalChapterMap.get(n.getChapterId()));
            if (n.getCourseId() != null) n.setCourseName(finalCourseMap.get(n.getCourseId()));
        });
    }

    private void enrichSingleNode(KnowledgeNode node) {
        if (node.getChapterId() != null) {
            Chapter chapter = chapterMapper.selectById(node.getChapterId());
            if (chapter != null) node.setChapterName(chapter.getChapterName());
        }
        if (node.getCourseId() != null) {
            Course course = courseMapper.selectById(node.getCourseId());
            if (course != null) node.setCourseName(course.getCourseName());
        }
    }
}

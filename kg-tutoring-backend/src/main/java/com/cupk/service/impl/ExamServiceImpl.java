package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.common.BusinessException;
import com.cupk.common.UserContext;
import com.cupk.mapper.CourseMapper;
import com.cupk.mapper.ExamMapper;
import com.cupk.mapper.ExamQuestionMapper;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.pojo.Course;
import com.cupk.pojo.Exam;
import com.cupk.pojo.ExamQuestion;
import com.cupk.pojo.ExamRecord;
import com.cupk.pojo.Question;
import com.cupk.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 测评定义服务实现
 */
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final CourseMapper courseMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<Map<String, Object>> listForTeacher(Integer courseId) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Exam::getCourseId, courseId);
        }
        wrapper.orderByDesc(Exam::getCreateTime);
        List<Exam> exams = examMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Exam exam : exams) {
            result.add(toExamRow(exam, null));
        }
        return result;
    }

    @Override
    @Transactional
    public void createExam(String examName, Integer courseId, List<Integer> questionIds, Integer totalScore) {
        if (examName == null || examName.isBlank()) {
            throw new BusinessException("测评名称不能为空");
        }
        if (courseId == null) {
            throw new BusinessException("课程不能为空");
        }
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("请至少选择一道题目");
        }

        Exam exam = new Exam();
        exam.setExamName(examName);
        exam.setCourseId(courseId);
        exam.setCreatorId(UserContext.getUserId());
        exam.setTotalScore(totalScore != null ? totalScore : questionIds.size());
        exam.setStatus(1);
        exam.setCreateTime(LocalDateTime.now());
        examMapper.insert(exam);

        int order = 1;
        for (Integer questionId : questionIds) {
            if (questionId == null) {
                continue;
            }
            ExamQuestion relation = new ExamQuestion();
            relation.setExamId(exam.getId());
            relation.setQuestionId(questionId);
            relation.setSortOrder(order++);
            examQuestionMapper.insert(relation);
        }
    }

    @Override
    @Transactional
    public void updateExam(Integer id, Map<String, Object> body) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("测评不存在");
        }
        if (body.containsKey("examName")) {
            exam.setExamName((String) body.get("examName"));
        }
        if (body.containsKey("totalScore")) {
            exam.setTotalScore((Integer) body.get("totalScore"));
        }
        if (body.containsKey("status")) {
            exam.setStatus((Integer) body.get("status"));
        }
        examMapper.updateById(exam);
    }

    @Override
    @Transactional
    public void deleteExam(Integer id) {
        // 级联删除：先删考试记录、题目关联，再删测评本身
        examRecordMapper.delete(new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id));
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, id));
        examMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listForStudent(Integer userId) {
        List<Exam> exams = examMapper.selectList(
                new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getStatus, 1)
                        .orderByDesc(Exam::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Exam exam : exams) {
            ExamRecord latestRecord = examRecordMapper.selectOne(
                    new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getUserId, userId)
                            .eq(ExamRecord::getExamId, exam.getId())
                            .orderByDesc(ExamRecord::getCreateTime)
                            .last("LIMIT 1"));
            result.add(toExamRow(exam, latestRecord));
        }
        return result;
    }

    @Override
    public Map<String, Object> getPaper(Integer examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null || exam.getStatus() == null || exam.getStatus() != 1) {
            throw new BusinessException("测评不存在或未发布");
        }
        Map<String, Object> paper = toExamRow(exam, null);
        List<ExamQuestion> relations = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId, examId)
                        .orderByAsc(ExamQuestion::getSortOrder));
        List<Map<String, Object>> questions = new ArrayList<>();
        for (ExamQuestion relation : relations) {
            Question question = questionMapper.selectById(relation.getQuestionId());
            if (question == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", question.getId());
            item.put("content", question.getContent());
            item.put("options", question.getOptions());
            item.put("difficulty", question.getDifficulty());
            item.put("questionType", question.getQuestionType());
            questions.add(item);
        }
        paper.put("questions", questions);
        return paper;
    }

    private Map<String, Object> toExamRow(Exam exam, ExamRecord latestRecord) {
        Map<String, Object> row = new LinkedHashMap<>();
        Course course = courseMapper.selectById(exam.getCourseId());
        Long questionCount = examQuestionMapper.selectCount(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, exam.getId()));

        row.put("id", exam.getId());
        row.put("examName", exam.getExamName());
        row.put("courseId", exam.getCourseId());
        row.put("courseName", course != null ? course.getCourseName() : null);
        row.put("totalScore", exam.getTotalScore());
        row.put("questionCount", questionCount);
        row.put("status", exam.getStatus());
        row.put("createTime", exam.getCreateTime());
        row.put("finished", latestRecord != null);
        if (latestRecord != null) {
            row.put("recordId", latestRecord.getId());
            row.put("score", latestRecord.getUserScore());
            row.put("userScore", latestRecord.getUserScore());
            row.put("passed", latestRecord.getUserScore() != null
                    && latestRecord.getUserScore().doubleValue() >= exam.getTotalScore() * 0.6);
            row.put("finishTime", latestRecord.getCreateTime());
        }
        return row;
    }
}

package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.StudyRecordMapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.ExamRecord;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.Question;
import com.cupk.pojo.StudyRecord;
import com.cupk.pojo.WrongQuestion;
import com.cupk.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 数据分析服务实现（教师/学生统计）
 */
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final StudyRecordMapper studyRecordMapper;
    private final ExamRecordMapper examRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final QuestionMapper questionMapper;

    @Override
    public Map<String, Object> classAnalysis(Integer courseId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 课程相关知识点总数
        Long totalNodes = knowledgeNodeMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeNode>()
                        .eq(KnowledgeNode::getCourseId, courseId));
        result.put("totalNodes", totalNodes);

        // 该课程下所有学习记录的去重学生数
        List<StudyRecord> allRecords = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>()
                        .inSql(StudyRecord::getNodeId,
                                "SELECT id FROM knowledge_node WHERE course_id = " + courseId));
        long studentCount = allRecords.stream()
                .map(StudyRecord::getUserId)
                .distinct().count();
        result.put("studentCount", studentCount);

        // 平均掌握度
        double avgMastery = allRecords.stream()
                .filter(r -> r.getMasteryLevel() != null)
                .mapToInt(StudyRecord::getMasteryLevel)
                .average().orElse(0);
        result.put("averageMastery", BigDecimal.valueOf(avgMastery).setScale(2, RoundingMode.HALF_UP));

        // 平均正确率
        double avgCorrect = allRecords.stream()
                .filter(r -> r.getCorrectRate() != null)
                .mapToDouble(r -> r.getCorrectRate().doubleValue())
                .average().orElse(0);
        result.put("averageCorrectRate", BigDecimal.valueOf(avgCorrect).setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    @Override
    public Map<String, Object> personalAnalysis(Integer userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 学习记录统计
        List<StudyRecord> records = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId));
        result.put("totalStudiedNodes", records.size());

        long masteredCount = records.stream()
                .filter(r -> r.getMasteryLevel() != null && r.getMasteryLevel() == 2)
                .count();
        result.put("masteredNodes", masteredCount);

        // 平均正确率
        double avgCorrectRate = records.stream()
                .filter(r -> r.getCorrectRate() != null)
                .mapToDouble(r -> r.getCorrectRate().doubleValue())
                .average()
                .orElse(0);
        result.put("averageCorrectRate", BigDecimal.valueOf(avgCorrectRate).setScale(2, RoundingMode.HALF_UP));

        // 总学习时长
        int totalMinutes = records.stream()
                .mapToInt(r -> r.getStudyMinutes() != null ? r.getStudyMinutes() : 0)
                .sum();
        result.put("totalStudyMinutes", totalMinutes);

        // 测评记录统计
        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId));
        result.put("totalExams", exams.size());

        // 错题数量
        Long wrongCount = wrongQuestionMapper.selectCount(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getUserId, userId));
        result.put("wrongQuestionCount", wrongCount);

        return result;
    }

    @Override
    public Map<String, Object> weakAnalysis(Integer userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 薄弱知识点：查询掌握度 < 2 的学习记录
        List<StudyRecord> weakRecords = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId)
                        .lt(StudyRecord::getMasteryLevel, 2));

        List<Map<String, Object>> weakNodes = new ArrayList<>();
        for (StudyRecord record : weakRecords) {
            KnowledgeNode node = knowledgeNodeMapper.selectById(record.getNodeId());
            if (node != null) {
                Map<String, Object> nodeInfo = new LinkedHashMap<>();
                nodeInfo.put("nodeId", node.getId());
                nodeInfo.put("nodeName", node.getName());
                nodeInfo.put("masteryLevel", record.getMasteryLevel());
                nodeInfo.put("correctRate", record.getCorrectRate());
                weakNodes.add(nodeInfo);
            }
        }
        result.put("weakNodes", weakNodes);
        result.put("weakCount", weakNodes.size());

        // 高频错题知识点（按错题数量降序）
        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(
                new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getUserId, userId));
        Map<Integer, Integer> nodeErrorCount = new LinkedHashMap<>();
        for (WrongQuestion wq : wrongQuestions) {
            Question q = questionMapper.selectById(wq.getQuestionId());
            if (q != null) {
                nodeErrorCount.merge(q.getNodeId(), wq.getWrongCount(), Integer::sum);
            }
        }
        List<Map<String, Object>> freqTopics = new ArrayList<>();
        nodeErrorCount.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    KnowledgeNode node = knowledgeNodeMapper.selectById(entry.getKey());
                    if (node != null) {
                        Map<String, Object> topic = new LinkedHashMap<>();
                        topic.put("nodeId", node.getId());
                        topic.put("nodeName", node.getName());
                        topic.put("errorCount", entry.getValue());
                        freqTopics.add(topic);
                    }
                });
        result.put("frequentWrongTopics", freqTopics);

        return result;
    }
}

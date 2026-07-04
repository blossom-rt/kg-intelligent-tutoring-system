package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.StudyRecordMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.ExamRecord;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.Question;
import com.cupk.pojo.StudyRecord;
import com.cupk.pojo.SysUser;
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
    private final SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> classAnalysis(Integer courseId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 课程相关知识点
        Long totalNodes = knowledgeNodeMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeNode>().eq(KnowledgeNode::getCourseId, courseId));
        result.put("totalNodes", totalNodes);

        // 该课程下所有学习记录
        List<StudyRecord> allRecords = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>()
                        .inSql(StudyRecord::getNodeId,
                                "SELECT id FROM knowledge_node WHERE course_id = " + courseId));
        result.put("totalRecords", allRecords.size());

        // 去重学生数 + 平均统计
        Set<Integer> studentIds = new HashSet<>();
        double sumMastery = 0, sumCorrect = 0;
        for (StudyRecord r : allRecords) {
            studentIds.add(r.getUserId());
            if (r.getMasteryLevel() != null) sumMastery += r.getMasteryLevel();
            if (r.getCorrectRate() != null) sumCorrect += r.getCorrectRate().doubleValue();
        }
        long totalStudents = studentIds.size();
        int masteryAvg = totalStudents > 0 ? (int) Math.round(sumMastery / allRecords.stream().filter(r -> r.getMasteryLevel() != null).count()) : 0;
        int correctAvg = totalStudents > 0 ? (int) Math.round(sumCorrect / allRecords.stream().filter(r -> r.getCorrectRate() != null).count()) : 0;
        result.put("totalStudents", totalStudents);
        result.put("avgMastery", masteryAvg);
        result.put("avgCorrectRate", correctAvg);
        result.put("activeStudents", totalStudents);

        // 学生明细列表
        List<Map<String, Object>> studentList = new ArrayList<>();
        for (Integer uid : studentIds) {
            SysUser user = sysUserMapper.selectById(uid);
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("userId", uid);
            s.put("studentName", user != null ? user.getRealName() : "学生" + uid);
            // 该学生在本课程下的统计
            long masterC = allRecords.stream().filter(r -> r.getUserId().equals(uid) && r.getMasteryLevel() != null && r.getMasteryLevel() >= 2).count();
            double cr = allRecords.stream().filter(r -> r.getUserId().equals(uid) && r.getCorrectRate() != null)
                    .mapToDouble(r -> r.getCorrectRate().doubleValue()).average().orElse(0);
            int min = allRecords.stream().filter(r -> r.getUserId().equals(uid) && r.getStudyMinutes() != null)
                    .mapToInt(StudyRecord::getStudyMinutes).sum();
            s.put("masteryLevel", (int) masterC);
            s.put("correctRate", (int) Math.round(cr));
            s.put("studyMinutes", min);
            studentList.add(s);
        }
        result.put("studentList", studentList);

        // 薄弱知识点 TOP5
        List<Map<String, Object>> weakNodes = new ArrayList<>();
        List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(
                new LambdaQueryWrapper<KnowledgeNode>().eq(KnowledgeNode::getCourseId, courseId));
        for (KnowledgeNode node : nodes) {
            double nodeAvg = allRecords.stream()
                    .filter(r -> r.getNodeId().equals(node.getId()) && r.getCorrectRate() != null)
                    .mapToDouble(r -> r.getCorrectRate().doubleValue()).average().orElse(100);
            if (nodeAvg < 70) {
                Map<String, Object> wn = new LinkedHashMap<>();
                wn.put("nodeName", node.getName());
                wn.put("masteryRate", (int) Math.round(nodeAvg));
                weakNodes.add(wn);
            }
        }
        weakNodes.sort((a, b) -> Integer.compare((int) a.get("masteryRate"), (int) b.get("masteryRate")));
        result.put("weakNodes", weakNodes.size() > 5 ? weakNodes.subList(0, 5) : weakNodes);

        // ===== 掌握度分布（用于饼图） =====
        int poor = 0, fair = 0, good = 0, excellent = 0;
        for (Integer uid : studentIds) {
            long masteredCount = allRecords.stream()
                    .filter(r -> r.getUserId().equals(uid) && r.getMasteryLevel() != null && r.getMasteryLevel() >= 2)
                    .count();
            double pct = totalNodes > 0 ? (masteredCount * 100.0 / totalNodes) : 0;
            if (pct < 30) poor++;
            else if (pct < 60) fair++;
            else if (pct < 80) good++;
            else excellent++;
        }
        List<Map<String, Object>> masteryDistribution = new ArrayList<>();
        Map<String, Object> d1 = new LinkedHashMap<>(); d1.put("name", "薄弱（<30%）"); d1.put("value", poor); masteryDistribution.add(d1);
        Map<String, Object> d2 = new LinkedHashMap<>(); d2.put("name", "一般（30%-60%）"); d2.put("value", fair); masteryDistribution.add(d2);
        Map<String, Object> d3 = new LinkedHashMap<>(); d3.put("name", "良好（60%-80%）"); d3.put("value", good); masteryDistribution.add(d3);
        Map<String, Object> d4 = new LinkedHashMap<>(); d4.put("name", "优秀（>=80%）"); d4.put("value", excellent); masteryDistribution.add(d4);
        result.put("masteryDistribution", masteryDistribution);

        // ===== 知识点平均正确率（用于柱状图） =====
        List<Map<String, Object>> nodeCorrectRates = new ArrayList<>();
        for (KnowledgeNode node : nodes) {
            double nodeAvg = allRecords.stream()
                    .filter(r -> r.getNodeId().equals(node.getId()) && r.getCorrectRate() != null)
                    .mapToDouble(r -> r.getCorrectRate().doubleValue())
                    .average()
                    .orElse(0);
            Map<String, Object> nc = new LinkedHashMap<>();
            nc.put("nodeName", node.getName());
            nc.put("correctRate", (int) Math.round(nodeAvg));
            nodeCorrectRates.add(nc);
        }
        // 按正确率排序
        nodeCorrectRates.sort((a, b) -> Integer.compare((int) b.get("correctRate"), (int) a.get("correctRate")));
        result.put("nodeCorrectRates", nodeCorrectRates);

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

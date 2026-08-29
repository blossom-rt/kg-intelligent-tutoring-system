package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cupk.mapper.ChapterMapper;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.StudyRecordMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.Chapter;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ChapterMapper chapterMapper;

    @Override
    public Map<String, Object> classAnalysis(Integer courseId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 课程相关知识点（先查节点，学习记录需按节点 ID 集合过滤）
        List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(
                new LambdaQueryWrapper<KnowledgeNode>().eq(KnowledgeNode::getCourseId, courseId));
        int totalNodes = nodes.size();
        result.put("totalNodes", totalNodes);

        // 该课程下所有学习记录（参数化 in 查询；无节点时直接短路，避免空 in 集合）
        List<Integer> nodeIds = nodes.stream().map(KnowledgeNode::getId).toList();
        List<StudyRecord> allRecords = nodeIds.isEmpty() ? List.of()
                : studyRecordMapper.selectList(
                        new LambdaQueryWrapper<StudyRecord>().in(StudyRecord::getNodeId, nodeIds));
        result.put("totalRecords", allRecords.size());

        // 单遍建立 节点→记录、学生→记录 两个索引，供下方各统计块复用
        Map<Integer, List<StudyRecord>> recordsByNode = new HashMap<>();
        Map<Integer, List<StudyRecord>> recordsByUser = new HashMap<>();
        for (StudyRecord r : allRecords) {
            recordsByNode.computeIfAbsent(r.getNodeId(), k -> new ArrayList<>()).add(r);
            recordsByUser.computeIfAbsent(r.getUserId(), k -> new ArrayList<>()).add(r);
        }
        Set<Integer> studentIds = new TreeSet<>(recordsByUser.keySet());
        long totalStudents = studentIds.size();

        // 每个学生的已掌握节点数（学生明细与掌握度分布共用）
        Map<Integer, Long> masteredCountByUser = new HashMap<>();
        for (Integer uid : studentIds) {
            masteredCountByUser.put(uid, recordsByUser.get(uid).stream()
                    .filter(r -> r.getMasteryLevel() != null && r.getMasteryLevel() >= 2)
                    .count());
        }

        // 平均掌握度：每学生"已掌握节点占比"的平均（百分比口径，与掌握度分布分档一致）
        int masteryAvg = 0;
        if (totalNodes > 0 && totalStudents > 0) {
            double sumPct = 0;
            for (Integer uid : studentIds) {
                sumPct += masteredCountByUser.get(uid) * 100.0 / totalNodes;
            }
            masteryAvg = (int) Math.round(sumPct / totalStudents);
        }
        int correctAvg = (int) Math.round(avgCorrectRate(allRecords, 0));
        result.put("totalStudents", totalStudents);
        result.put("avgMastery", masteryAvg);
        result.put("avgCorrectRate", correctAvg);
        result.put("activeStudents", totalStudents);

        // 学生姓名批量查询
        Map<Integer, SysUser> usersById = studentIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(studentIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 学生明细列表
        result.put("studentList", buildStudentStats(studentIds, recordsByUser, masteredCountByUser, usersById));

        // 薄弱知识点 TOP5
        result.put("weakNodes", buildWeakNodes(nodes, recordsByNode));

        // 掌握度分布（用于饼图）
        result.put("masteryDistribution", buildMasteryDistribution(studentIds, masteredCountByUser, totalNodes));

        // 知识点平均正确率（用于柱状图）
        result.put("nodeCorrectRates", buildNodeCorrectRates(nodes, recordsByNode));

        // 学习趋势（最近7天每日学习人数）
        result.put("studyTrend", buildStudyTrend(allRecords));

        // 薄弱知识点排行（按正确率升序）
        result.put("weakRank", buildWeakRank(nodes, recordsByNode));

        // 章节掌握度（按章节分组的平均正确率）
        List<Chapter> chapters = chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>().eq(Chapter::getCourseId, courseId).orderByAsc(Chapter::getSort));
        result.put("chapterMastery", buildChapterMastery(chapters, nodes, recordsByNode));

        return result;
    }

    /**
     * 记录列表中非空 correctRate 的平均值，无有效记录时返回 defaultIfEmpty
     */
    private double avgCorrectRate(List<StudyRecord> records, double defaultIfEmpty) {
        return records.stream()
                .filter(r -> r.getCorrectRate() != null)
                .mapToDouble(r -> r.getCorrectRate().doubleValue())
                .average()
                .orElse(defaultIfEmpty);
    }

    private List<Map<String, Object>> buildStudentStats(Set<Integer> studentIds,
                                                        Map<Integer, List<StudyRecord>> recordsByUser,
                                                        Map<Integer, Long> masteredCountByUser,
                                                        Map<Integer, SysUser> usersById) {
        List<Map<String, Object>> studentList = new ArrayList<>();
        for (Integer uid : studentIds) {
            List<StudyRecord> rs = recordsByUser.getOrDefault(uid, List.of());
            SysUser user = usersById.get(uid);
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("userId", uid);
            s.put("studentName", user != null ? user.getRealName() : "学生" + uid);
            s.put("masteryLevel", masteredCountByUser.get(uid).intValue());
            s.put("correctRate", (int) Math.round(avgCorrectRate(rs, 0)));
            s.put("studyMinutes", rs.stream()
                    .filter(r -> r.getStudyMinutes() != null)
                    .mapToInt(StudyRecord::getStudyMinutes).sum());
            studentList.add(s);
        }
        return studentList;
    }

    private List<Map<String, Object>> buildWeakNodes(List<KnowledgeNode> nodes,
                                                     Map<Integer, List<StudyRecord>> recordsByNode) {
        List<Map<String, Object>> weakNodes = new ArrayList<>();
        for (KnowledgeNode node : nodes) {
            // 无有效记录的节点视为 100 分，不进入薄弱列表
            double nodeAvg = avgCorrectRate(recordsByNode.getOrDefault(node.getId(), List.of()), 100);
            if (nodeAvg < 70) {
                Map<String, Object> wn = new LinkedHashMap<>();
                wn.put("nodeName", node.getName());
                wn.put("masteryRate", (int) Math.round(nodeAvg));
                weakNodes.add(wn);
            }
        }
        weakNodes.sort((a, b) -> Integer.compare((int) a.get("masteryRate"), (int) b.get("masteryRate")));
        return weakNodes.size() > 5 ? new ArrayList<>(weakNodes.subList(0, 5)) : weakNodes;
    }

    private List<Map<String, Object>> buildMasteryDistribution(Set<Integer> studentIds,
                                                               Map<Integer, Long> masteredCountByUser,
                                                               int totalNodes) {
        int poor = 0, fair = 0, good = 0, excellent = 0;
        for (Integer uid : studentIds) {
            double pct = totalNodes > 0 ? (masteredCountByUser.get(uid) * 100.0 / totalNodes) : 0;
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
        return masteryDistribution;
    }

    private List<Map<String, Object>> buildNodeCorrectRates(List<KnowledgeNode> nodes,
                                                            Map<Integer, List<StudyRecord>> recordsByNode) {
        List<Map<String, Object>> nodeCorrectRates = new ArrayList<>();
        for (KnowledgeNode node : nodes) {
            // 无有效记录的节点正确率记 0
            double nodeAvg = avgCorrectRate(recordsByNode.getOrDefault(node.getId(), List.of()), 0);
            Map<String, Object> nc = new LinkedHashMap<>();
            nc.put("nodeName", node.getName());
            nc.put("correctRate", (int) Math.round(nodeAvg));
            nodeCorrectRates.add(nc);
        }
        nodeCorrectRates.sort((a, b) -> Integer.compare((int) b.get("correctRate"), (int) a.get("correctRate")));
        return nodeCorrectRates;
    }

    private List<Map<String, Object>> buildStudyTrend(List<StudyRecord> allRecords) {
        Map<LocalDate, Long> countsByDay = new HashMap<>();
        for (StudyRecord r : allRecords) {
            LocalDateTime updateTime = r.getUpdateTime();
            if (updateTime != null) {
                countsByDay.merge(updateTime.toLocalDate(), 1L, Long::sum);
            }
        }
        List<Map<String, Object>> studyTrend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("date", day.toString());
            d.put("studyCount", countsByDay.getOrDefault(day, 0L));
            studyTrend.add(d);
        }
        return studyTrend;
    }

    private List<Map<String, Object>> buildWeakRank(List<KnowledgeNode> nodes,
                                                    Map<Integer, List<StudyRecord>> recordsByNode) {
        List<Map<String, Object>> weakRank = new ArrayList<>();
        for (KnowledgeNode node : nodes) {
            // 无有效记录的节点视为 100 分，排在末尾
            double nodeAvg = avgCorrectRate(recordsByNode.getOrDefault(node.getId(), List.of()), 100);
            Map<String, Object> wr = new LinkedHashMap<>();
            wr.put("nodeName", node.getName());
            wr.put("correctRate", (int) Math.round(nodeAvg));
            wr.put("studentCount", recordsByNode.getOrDefault(node.getId(), List.of()).size());
            weakRank.add(wr);
        }
        weakRank.sort((a, b) -> Integer.compare((int) a.get("correctRate"), (int) b.get("correctRate")));
        return weakRank.size() > 10 ? new ArrayList<>(weakRank.subList(0, 10)) : weakRank;
    }

    private List<Map<String, Object>> buildChapterMastery(List<Chapter> chapters,
                                                          List<KnowledgeNode> nodes,
                                                          Map<Integer, List<StudyRecord>> recordsByNode) {
        Map<Integer, List<StudyRecord>> recordsByChapter = new HashMap<>();
        for (KnowledgeNode node : nodes) {
            if (node.getChapterId() != null) {
                recordsByChapter.computeIfAbsent(node.getChapterId(), k -> new ArrayList<>())
                        .addAll(recordsByNode.getOrDefault(node.getId(), List.of()));
            }
        }
        List<Map<String, Object>> chapterMastery = new ArrayList<>();
        for (Chapter ch : chapters) {
            List<StudyRecord> crs = recordsByChapter.getOrDefault(ch.getId(), Collections.emptyList());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("chapterId", ch.getId());
            item.put("chapterName", ch.getChapterName());
            item.put("chapterSort", ch.getSort());
            item.put("avgCorrectRate", (int) Math.round(avgCorrectRate(crs, 0)));
            item.put("studentCount", crs.stream().map(StudyRecord::getUserId).distinct().count());
            chapterMastery.add(item);
        }
        return chapterMastery;
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

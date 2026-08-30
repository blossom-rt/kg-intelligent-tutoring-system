package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cupk.mapper.ChapterMapper;
import com.cupk.mapper.ExamRecordMapper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.mapper.StudyRecordMapper;
import com.cupk.mapper.SysUserMapper;
import com.cupk.mapper.WrongQuestionMapper;
import com.cupk.pojo.Chapter;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.StudyRecord;
import com.cupk.pojo.SysUser;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * classAnalysis 统计口径与重构回归测试（纯 Mockito，不依赖数据库）
 */
@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock private StudyRecordMapper studyRecordMapper;
    @Mock private ExamRecordMapper examRecordMapper;
    @Mock private WrongQuestionMapper wrongQuestionMapper;
    @Mock private KnowledgeNodeMapper knowledgeNodeMapper;
    @Mock private QuestionMapper questionMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private ChapterMapper chapterMapper;

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    @BeforeAll
    static void initEntityMetadata() {
        // 纯单元测试环境下 MyBatis-Plus 未启动，需手动注册实体的列名缓存，
        // 否则 Service 内构建 LambdaQueryWrapper 时无法解析 lambda 对应的列名
        initIfAbsent(KnowledgeNode.class, StudyRecord.class, SysUser.class, Chapter.class);
    }

    private static void initIfAbsent(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (TableInfoHelper.getTableInfo(clazz) == null) {
                TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), clazz);
            }
        }
    }

    private static KnowledgeNode node(int id, int courseId, Integer chapterId, String name) {
        KnowledgeNode n = new KnowledgeNode();
        n.setId(id);
        n.setCourseId(courseId);
        n.setChapterId(chapterId);
        n.setName(name);
        return n;
    }

    private static Chapter chapter(int id, String name, int sort) {
        Chapter c = new Chapter();
        c.setId(id);
        c.setChapterName(name);
        c.setSort(sort);
        return c;
    }

    private static StudyRecord rec(int userId, int nodeId, Integer masteryLevel,
                                   Double correctRate, Integer studyMinutes, LocalDateTime updateTime) {
        StudyRecord r = new StudyRecord();
        r.setUserId(userId);
        r.setNodeId(nodeId);
        r.setMasteryLevel(masteryLevel);
        r.setCorrectRate(correctRate == null ? null : BigDecimal.valueOf(correctRate));
        r.setStudyMinutes(studyMinutes);
        r.setUpdateTime(updateTime);
        return r;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> byName(List<Map<String, Object>> list, String name) {
        return list.stream().filter(m -> name.equals(m.get("name"))).findFirst().orElse(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void classAnalysisComputesAllKeys() {
        KnowledgeNode n1 = node(1, 10, 101, "节点一");
        KnowledgeNode n2 = node(2, 10, 101, "节点二");
        KnowledgeNode n3 = node(3, 10, 102, "节点三");
        when(knowledgeNodeMapper.selectList(any())).thenReturn(List.of(n1, n2, n3));

        LocalDate today = LocalDate.now();
        LocalDateTime yesterday = today.minusDays(1).atTime(10, 0);
        when(studyRecordMapper.selectList(any())).thenReturn(List.of(
                rec(1, 1, 2, 80.0, 30, today.atTime(10, 0)),   // u1 节点一 已掌握
                rec(1, 2, 1, 50.0, 20, today.atTime(9, 0)),    // u1 节点二 学习中
                rec(2, 1, 2, 90.0, 40, yesterday),             // u2 节点一 已掌握（昨日）
                rec(2, 3, 0, null, 10, null),                  // u2 节点三 未学，无正确率
                rec(3, 2, null, 60.0, null, null),             // u3 节点二 无掌握度
                rec(2, 2, 2, 70.0, 15, today.atTime(8, 0))     // u2 节点二 已掌握
        ));

        Chapter c1 = chapter(101, "第一章", 1);
        Chapter c2 = chapter(102, "第二章", 2);
        when(chapterMapper.selectList(any())).thenReturn(List.of(c1, c2));

        SysUser u1 = new SysUser();
        u1.setId(1);
        u1.setRealName("张三");
        SysUser u2 = new SysUser();
        u2.setId(2);
        u2.setRealName("李四");
        when(sysUserMapper.selectBatchIds(anyCollection())).thenReturn(List.of(u1, u2));

        Map<String, Object> result = analysisService.classAnalysis(10);

        // 基础计数
        assertThat(result.get("totalNodes")).isEqualTo(3);
        assertThat(result.get("totalRecords")).isEqualTo(6);
        assertThat(result.get("totalStudents")).isEqualTo(3L);
        assertThat(result.get("activeStudents")).isEqualTo(3L);

        // 平均正确率 = (80+50+90+60+70)/5 = 70
        assertThat(result.get("avgCorrectRate")).isEqualTo(70);
        // 平均掌握度 = 各学生已掌握节点占比的平均 = (1/3 + 2/3 + 0)/3 = 33%
        assertThat(result.get("avgMastery")).isEqualTo(33);

        // 学生明细：按学号升序；u3 查不到姓名时回退"学生3"
        List<Map<String, Object>> studentList = (List<Map<String, Object>>) result.get("studentList");
        assertThat(studentList).hasSize(3);
        assertThat(studentList.get(0)).containsEntry("userId", 1).containsEntry("studentName", "张三")
                .containsEntry("masteryLevel", 1).containsEntry("correctRate", 65).containsEntry("studyMinutes", 50);
        assertThat(studentList.get(1)).containsEntry("userId", 2).containsEntry("studentName", "李四")
                .containsEntry("masteryLevel", 2).containsEntry("correctRate", 80).containsEntry("studyMinutes", 65);
        assertThat(studentList.get(2)).containsEntry("userId", 3).containsEntry("studentName", "学生3")
                .containsEntry("masteryLevel", 0).containsEntry("correctRate", 60).containsEntry("studyMinutes", 0);

        // 薄弱知识点：节点二正确率 60 < 70；节点三无有效记录视为 100 不入选
        List<Map<String, Object>> weakNodes = (List<Map<String, Object>>) result.get("weakNodes");
        assertThat(weakNodes).hasSize(1);
        assertThat(weakNodes.get(0)).containsEntry("nodeName", "节点二").containsEntry("masteryRate", 60);

        // 掌握度分布：u1 33%→一般，u2 67%→良好，u3 0%→薄弱
        List<Map<String, Object>> distribution = (List<Map<String, Object>>) result.get("masteryDistribution");
        assertThat(byName(distribution, "薄弱（<30%）")).containsEntry("value", 1);
        assertThat(byName(distribution, "一般（30%-60%）")).containsEntry("value", 1);
        assertThat(byName(distribution, "良好（60%-80%）")).containsEntry("value", 1);
        assertThat(byName(distribution, "优秀（>=80%）")).containsEntry("value", 0);

        // 知识点正确率（柱状图）：无记录的节点三记 0，降序
        List<Map<String, Object>> nodeRates = (List<Map<String, Object>>) result.get("nodeCorrectRates");
        assertThat(nodeRates).containsExactly(
                Map.of("nodeName", "节点一", "correctRate", 85),
                Map.of("nodeName", "节点二", "correctRate", 60),
                Map.of("nodeName", "节点三", "correctRate", 0));

        // 学习趋势：最近 7 天，今日 3 条（r1/r2/r6），昨日 1 条（r3），无时间的记录不计入
        List<Map<String, Object>> trend = (List<Map<String, Object>>) result.get("studyTrend");
        assertThat(trend).hasSize(7);
        assertThat(trend.get(6)).containsEntry("date", today.toString()).containsEntry("studyCount", 3L);
        assertThat(trend.get(5)).containsEntry("date", today.minusDays(1).toString()).containsEntry("studyCount", 1L);
        assertThat(trend.get(0)).containsEntry("studyCount", 0L);

        // 薄弱排行：升序，含学习人数；节点三无有效记录记 100 排末尾
        List<Map<String, Object>> weakRank = (List<Map<String, Object>>) result.get("weakRank");
        assertThat(weakRank).containsExactly(
                Map.of("nodeName", "节点二", "correctRate", 60, "studentCount", 3),
                Map.of("nodeName", "节点一", "correctRate", 85, "studentCount", 2),
                Map.of("nodeName", "节点三", "correctRate", 100, "studentCount", 1));

        // 章节掌握度：第一章聚合节点一二共 5 条有效正确率 (80+90+50+60+70)/5=70，学生 3 人
        List<Map<String, Object>> chapterMastery = (List<Map<String, Object>>) result.get("chapterMastery");
        assertThat(chapterMastery).hasSize(2);
        assertThat(chapterMastery.get(0)).containsEntry("chapterId", 101).containsEntry("chapterName", "第一章")
                .containsEntry("chapterSort", 1).containsEntry("avgCorrectRate", 70).containsEntry("studentCount", 3L);
        assertThat(chapterMastery.get(1)).containsEntry("chapterId", 102)
                .containsEntry("avgCorrectRate", 0).containsEntry("studentCount", 1L);

        // P0 回归：学习记录查询必须按节点 ID 参数化，不得出现 SQL 字符串拼接
        ArgumentCaptor<LambdaQueryWrapper<StudyRecord>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(studyRecordMapper).selectList(captor.capture());
        LambdaQueryWrapper<StudyRecord> wrapper = captor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("IN");
        assertThat(wrapper.getSqlSegment()).doesNotContain("SELECT id FROM knowledge_node");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void classAnalysisWithoutNodesShortcircuits() {
        when(knowledgeNodeMapper.selectList(any())).thenReturn(List.of());
        when(chapterMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> result = analysisService.classAnalysis(99);

        assertThat(result.get("totalNodes")).isEqualTo(0);
        assertThat(result.get("totalRecords")).isEqualTo(0);
        assertThat(result.get("totalStudents")).isEqualTo(0L);
        assertThat(result.get("avgMastery")).isEqualTo(0);
        assertThat(result.get("avgCorrectRate")).isEqualTo(0);
        assertThat((List<?>) result.get("studentList")).isEmpty();
        assertThat((List<?>) result.get("weakNodes")).isEmpty();
        assertThat((List<?>) result.get("weakRank")).isEmpty();

        List<Map<String, Object>> trend = (List<Map<String, Object>>) result.get("studyTrend");
        assertThat(trend).hasSize(7);
        assertThat(trend.stream().mapToInt(m -> ((Number) m.get("studyCount")).intValue()).sum()).isZero();

        // 无节点时不应再查学习记录，也不应批量查学生
        verify(studyRecordMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(sysUserMapper, never()).selectBatchIds(anyCollection());
    }

    @Test
    @SuppressWarnings("unchecked")
    void weakNodesTruncatedToTopFive() {
        List<KnowledgeNode> nodes = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            nodes.add(node(i, 10, null, "节点" + i));
        }
        when(knowledgeNodeMapper.selectList(any())).thenReturn(nodes);
        // 7 个节点正确率分别为 10,20,30,40,50,60,70 → 前 6 个 <70，取升序前 5
        when(studyRecordMapper.selectList(any())).thenReturn(nodes.stream()
                .map(n -> rec(1, n.getId(), 2, n.getId() * 10.0, 10, null))
                .collect(Collectors.toList()));
        when(chapterMapper.selectList(any())).thenReturn(List.of());
        when(sysUserMapper.selectBatchIds(anyCollection())).thenReturn(List.of());

        Map<String, Object> result = analysisService.classAnalysis(10);

        List<Map<String, Object>> weakNodes = (List<Map<String, Object>>) result.get("weakNodes");
        assertThat(weakNodes).hasSize(5);
        assertThat(weakNodes.stream().map(m -> m.get("masteryRate")).collect(Collectors.toList()))
                .containsExactly(10, 20, 30, 40, 50);
    }

    @Test
    @SuppressWarnings("unchecked")
    void studentStatsHandleNullRates() {
        KnowledgeNode n1 = node(1, 10, null, "节点一");
        when(knowledgeNodeMapper.selectList(any())).thenReturn(List.of(n1));
        // 该学生的全部记录都没有正确率：明细正确率应记 0 而不是 NaN
        when(studyRecordMapper.selectList(any())).thenReturn(List.of(
                rec(5, 1, 0, null, 25, null)));
        when(chapterMapper.selectList(any())).thenReturn(List.of());
        when(sysUserMapper.selectBatchIds(anyCollection())).thenReturn(List.of());

        Map<String, Object> result = analysisService.classAnalysis(10);

        assertThat(result.get("avgCorrectRate")).isEqualTo(0);
        List<Map<String, Object>> studentList = (List<Map<String, Object>>) result.get("studentList");
        assertThat(studentList.get(0)).containsEntry("correctRate", 0).containsEntry("studyMinutes", 25);
    }
}

package com.cupk.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cupk.mapper.KnowledgeNodeMapper;
import com.cupk.mapper.QuestionMapper;
import com.cupk.pojo.KnowledgeNode;
import com.cupk.pojo.Question;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 习题列表查询的过滤条件与回归测试（重点：courseId 过滤不得再拼接 SQL）
 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock private QuestionMapper questionMapper;
    @Mock private KnowledgeNodeMapper knowledgeNodeMapper;

    @InjectMocks
    private QuestionServiceImpl questionService;

    @BeforeAll
    static void initEntityMetadata() {
        initIfAbsent(Question.class, KnowledgeNode.class);
    }

    private static void initIfAbsent(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (TableInfoHelper.getTableInfo(clazz) == null) {
                TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), clazz);
            }
        }
    }

    private static Question question(int id, int nodeId) {
        Question q = new Question();
        q.setId(id);
        q.setNodeId(nodeId);
        q.setContent("题目" + id);
        return q;
    }

    private static KnowledgeNode node(int id, String name) {
        KnowledgeNode n = new KnowledgeNode();
        n.setId(id);
        n.setName(name);
        return n;
    }

    @Test
    @SuppressWarnings("unchecked")
    void listByCourseUsesParameterizedNodeIds() {
        when(knowledgeNodeMapper.selectList(any()))
                .thenReturn(List.of(node(1, "甲"), node(2, "乙")));
        Question q1 = question(11, 1);
        Question q2 = question(12, 2);
        when(questionMapper.selectList(any())).thenReturn(new java.util.ArrayList<>(List.of(q1, q2)));
        when(knowledgeNodeMapper.selectById(1)).thenReturn(node(1, "甲"));
        when(knowledgeNodeMapper.selectById(2)).thenReturn(node(2, "乙"));

        List<Question> result = questionService.list(null, 5, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNodeName()).isEqualTo("甲");
        assertThat(result.get(1).getNodeName()).isEqualTo("乙");

        // P0 回归：courseId 过滤必须是"先查节点 ID 再参数化 IN"，不得拼接 SQL 字符串
        ArgumentCaptor<LambdaQueryWrapper<Question>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(questionMapper).selectList(captor.capture());
        LambdaQueryWrapper<Question> wrapper = captor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("IN");
        assertThat(wrapper.getSqlSegment()).doesNotContain("SELECT id FROM knowledge_node");
        Collection<Object> params = wrapper.getParamNameValuePairs().values();
        assertThat(params).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void listByCourseWithoutNodesReturnsEmpty() {
        when(knowledgeNodeMapper.selectList(any())).thenReturn(List.of());

        List<Question> result = questionService.list(null, 5, null);

        assertThat(result).isEmpty();
        // 课程下没有节点时不应查询习题表，也不应生成空 IN 集合的非法 SQL
        verify(questionMapper, never()).selectList(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listCombinesNodeIdAndDifficultyFilters() {
        when(questionMapper.selectList(any())).thenReturn(new java.util.ArrayList<>());

        List<Question> result = questionService.list(7, null, 3);

        assertThat(result).isEmpty();
        ArgumentCaptor<LambdaQueryWrapper<Question>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(questionMapper).selectList(captor.capture());
        LambdaQueryWrapper<Question> wrapper = captor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("node_id =").contains("difficulty =");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactlyInAnyOrder(7, 3);
        // 未按课程过滤时不应查询节点表
        verify(knowledgeNodeMapper, never()).selectList(any());
    }
}

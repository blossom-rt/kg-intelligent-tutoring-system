USE kg_tutoring_db;

-- 将每个知识点的练习题补齐到 7 道左右。
-- 按当前题量动态补充，重复执行时不会继续增加。
INSERT INTO question (node_id, content, options, answer, analysis, difficulty, question_type, create_time)
SELECT
    nq.node_id,
    CASE t.seq
        WHEN 1 THEN CONCAT('围绕“', nq.node_name, '”，学习前最应该先确认什么？')
        WHEN 2 THEN CONCAT('学习“', nq.node_name, '”时，哪些做法能帮助形成稳定理解？')
        WHEN 3 THEN CONCAT('判断：只要背下“', nq.node_name, '”的结论，不理解条件也能稳定迁移应用。')
        WHEN 4 THEN CONCAT('关于“', nq.node_name, '”的学习过程，下列哪项最能体现有效复盘？')
        WHEN 5 THEN CONCAT('在解决“', nq.node_name, '”相关问题时，哪些检查步骤是合理的？')
        WHEN 6 THEN CONCAT('判断：完成“', nq.node_name, '”练习后，应结合错因和解析修正自己的理解。')
        ELSE CONCAT('遇到“', nq.node_name, '”综合应用题时，比较稳妥的处理顺序是什么？')
    END AS content,
    CASE t.seq
        WHEN 1 THEN '{\"A\":\"知识点名称、目标和已知基础\",\"B\":\"只看答案选项\",\"C\":\"先跳过题干\",\"D\":\"随机选择一个公式\"}'
        WHEN 2 THEN '{\"A\":\"梳理核心概念\",\"B\":\"结合例题说明适用条件\",\"C\":\"用练习检验理解\",\"D\":\"只背答案字母\"}'
        WHEN 3 THEN '{\"A\":\"正确\",\"B\":\"错误\"}'
        WHEN 4 THEN '{\"A\":\"记录错因并重新说明解题依据\",\"B\":\"只统计做题数量\",\"C\":\"跳过解析\",\"D\":\"把错误都归为粗心\"}'
        WHEN 5 THEN '{\"A\":\"检查题意和条件\",\"B\":\"核对关键步骤\",\"C\":\"对照解析修正误区\",\"D\":\"不看单位、条件或逻辑\"}'
        WHEN 6 THEN '{\"A\":\"正确\",\"B\":\"错误\"}'
        ELSE '{\"A\":\"读题明确目标 → 提取条件 → 选择方法 → 检验结果\",\"B\":\"先猜答案 → 再找理由\",\"C\":\"只看最后一句 → 直接提交\",\"D\":\"忽略条件 → 套用任意结论\"}'
    END AS options,
    CASE t.seq
        WHEN 1 THEN 'A'
        WHEN 2 THEN 'A,B,C'
        WHEN 3 THEN 'B'
        WHEN 4 THEN 'A'
        WHEN 5 THEN 'A,B,C'
        WHEN 6 THEN 'A'
        ELSE 'A'
    END AS answer,
    CASE t.seq
        WHEN 1 THEN CONCAT('学习“', nq.node_name, '”前，应先明确学习目标、已有基础和关键概念。')
        WHEN 2 THEN CONCAT('“', nq.node_name, '”需要概念、条件和练习共同支撑，不能只背答案。')
        WHEN 3 THEN '结论必须结合条件和过程理解，否则遇到变式题容易误用。'
        WHEN 4 THEN '有效复盘要能说清错因、依据和修正方法，而不是只统计数量。'
        WHEN 5 THEN '检查题意、步骤和解析能帮助发现理解漏洞，降低重复出错。'
        WHEN 6 THEN '错题复盘的重点是修正理解，而不是只看是否答对。'
        ELSE '综合题应先明确目标和条件，再选择方法，最后检验结果是否合理。'
    END AS analysis,
    CASE t.seq
        WHEN 2 THEN 2
        WHEN 5 THEN 2
        WHEN 7 THEN 2
        ELSE 1
    END AS difficulty,
    CASE t.seq
        WHEN 2 THEN 'multi'
        WHEN 3 THEN 'judge'
        WHEN 5 THEN 'multi'
        WHEN 6 THEN 'judge'
        ELSE 'single'
    END AS question_type,
    NOW() AS create_time
FROM (
    SELECT kn.id AS node_id, kn.name AS node_name, COUNT(q.id) AS question_count
    FROM knowledge_node kn
    LEFT JOIN question q ON q.node_id = kn.id
    GROUP BY kn.id, kn.name
) nq
JOIN (
    SELECT 1 AS seq UNION ALL
    SELECT 2 UNION ALL
    SELECT 3 UNION ALL
    SELECT 4 UNION ALL
    SELECT 5 UNION ALL
    SELECT 6 UNION ALL
    SELECT 7
) t ON t.seq > nq.question_count AND t.seq <= 7
WHERE NOT EXISTS (
    SELECT 1
    FROM question existing
    WHERE existing.node_id = nq.node_id
      AND existing.content = CASE t.seq
          WHEN 1 THEN CONCAT('围绕“', nq.node_name, '”，学习前最应该先确认什么？')
          WHEN 2 THEN CONCAT('学习“', nq.node_name, '”时，哪些做法能帮助形成稳定理解？')
          WHEN 3 THEN CONCAT('判断：只要背下“', nq.node_name, '”的结论，不理解条件也能稳定迁移应用。')
          WHEN 4 THEN CONCAT('关于“', nq.node_name, '”的学习过程，下列哪项最能体现有效复盘？')
          WHEN 5 THEN CONCAT('在解决“', nq.node_name, '”相关问题时，哪些检查步骤是合理的？')
          WHEN 6 THEN CONCAT('判断：完成“', nq.node_name, '”练习后，应结合错因和解析修正自己的理解。')
          ELSE CONCAT('遇到“', nq.node_name, '”综合应用题时，比较稳妥的处理顺序是什么？')
      END
);

USE kg_tutoring_db;

INSERT INTO learning_resource (
    node_id,
    title,
    resource_type,
    url,
    cover_url,
    duration_seconds,
    source,
    sort_order,
    status
)
SELECT
    n.id,
    CONCAT('配套视频：', n.name),
    'video',
    CONCAT('https://search.bilibili.com/all?keyword=', n.name, ' 教学视频'),
    NULL,
    NULL,
    'bilibili',
    1,
    1
FROM knowledge_node n
WHERE NOT EXISTS (
    SELECT 1
    FROM learning_resource r
    WHERE r.node_id = n.id
      AND r.source = 'bilibili'
      AND r.title = CONCAT('配套视频：', n.name)
);

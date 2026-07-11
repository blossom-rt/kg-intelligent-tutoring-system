USE kg_tutoring_db;

CREATE TABLE IF NOT EXISTS chapter (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '章节ID',
    course_id INT NOT NULL COMMENT '所属课程ID',
    chapter_name VARCHAR(60) NOT NULL COMMENT '章节名称',
    sort INT DEFAULT 1 COMMENT '章节排序号',
    description VARCHAR(200) DEFAULT NULL COMMENT '章节教学目标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_course_id (course_id),
    CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程章节表';

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE knowledge_node ADD COLUMN chapter_id INT NULL AFTER course_id, ADD KEY idx_chapter_id (chapter_id), ADD CONSTRAINT fk_node_chapter FOREIGN KEY (chapter_id) REFERENCES chapter(id)',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'knowledge_node' AND COLUMN_NAME = 'chapter_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_chapter_column = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'knowledge_node' AND COLUMN_NAME = 'chapter'
);

SET @sql = IF(
    @has_chapter_column = 1,
    'INSERT INTO chapter (course_id, chapter_name, sort)
     SELECT DISTINCT n.course_id, COALESCE(NULLIF(n.chapter, ''''), ''未分章''), 1
     FROM knowledge_node n
     WHERE NOT EXISTS (
         SELECT 1
         FROM chapter c
         WHERE c.course_id = n.course_id
           AND c.chapter_name = COALESCE(NULLIF(n.chapter, ''''), ''未分章'')
     )',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    @has_chapter_column = 1,
    'UPDATE knowledge_node n
     INNER JOIN chapter c
       ON n.course_id = c.course_id
      AND c.chapter_name = COALESCE(NULLIF(n.chapter, ''''), ''未分章'')
     SET n.chapter_id = c.id
     WHERE n.chapter_id IS NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE knowledge_node MODIFY COLUMN chapter_id INT NOT NULL;

SET @sql = IF(
    @has_chapter_column = 1,
    'ALTER TABLE knowledge_node DROP COLUMN chapter',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

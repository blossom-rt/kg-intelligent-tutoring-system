USE kg_tutoring_db;

-- 1. 新建章节表（幂等，已存在则跳过）
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

-- 2. 给知识点表新增 chapter_id 字段（先允许为空，避免旧数据报错）
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

-- 3. 自动迁移：从旧 chapter 文本字段提取「课程-章节」，生成章节表数据
INSERT IGNORE INTO chapter (course_id, chapter_name, sort)
SELECT DISTINCT course_id, chapter, 1
FROM knowledge_node
WHERE chapter IS NOT NULL AND chapter != '';

-- 4. 自动回填：给知识点表的 chapter_id 赋值，关联上新生成的章节
UPDATE knowledge_node n
INNER JOIN chapter c ON n.course_id = c.course_id AND n.chapter = c.chapter_name
SET n.chapter_id = c.id
WHERE n.chapter_id IS NULL AND n.chapter IS NOT NULL;
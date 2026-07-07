USE kg_tutoring_db;

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE knowledge_node ADD COLUMN node_type VARCHAR(20) DEFAULT ''concept'' COMMENT ''节点类型：concept概念 skill技能 application应用'' AFTER description',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'knowledge_node' AND COLUMN_NAME = 'node_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE knowledge_node ADD COLUMN learning_goal VARCHAR(200) DEFAULT NULL COMMENT ''学习目标'' AFTER node_type',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'knowledge_node' AND COLUMN_NAME = 'learning_goal'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE knowledge_node ADD COLUMN keywords VARCHAR(200) DEFAULT NULL COMMENT ''关键词，逗号分隔'' AFTER learning_goal',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'knowledge_node' AND COLUMN_NAME = 'keywords'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE knowledge_node ADD COLUMN example_hint VARCHAR(300) DEFAULT NULL COMMENT ''例题或学习提示'' AFTER keywords',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'knowledge_node' AND COLUMN_NAME = 'example_hint'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

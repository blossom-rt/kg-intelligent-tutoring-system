-- 5. 全部数据迁移完成后，把 chapter_id 改为非空约束
ALTER TABLE knowledge_node MODIFY COLUMN chapter_id INT NOT NULL;

-- 6. 删除废弃的旧 chapter 文本字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 1,
        'ALTER TABLE knowledge_node DROP COLUMN chapter',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'knowledge_node' AND COLUMN_NAME = 'chapter'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
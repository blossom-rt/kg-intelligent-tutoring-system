USE kg_tutoring_db;

SET @schema_name = DATABASE();
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE exam_record ADD COLUMN start_time DATETIME DEFAULT NULL COMMENT ''开始答题时间'' AFTER ai_report',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'exam_record' AND COLUMN_NAME = 'start_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

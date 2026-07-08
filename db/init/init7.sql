USE kg_tutoring_db;
ALTER TABLE exam_record ADD COLUMN start_time DATETIME DEFAULT NULL COMMENT '开始答题时间' AFTER ai_report;
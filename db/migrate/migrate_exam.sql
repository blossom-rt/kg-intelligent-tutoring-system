USE kg_tutoring_db;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS exam (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '测评ID',
    exam_name VARCHAR(100) NOT NULL COMMENT '测评名称',
    course_id INT NOT NULL COMMENT '所属课程ID',
    creator_id INT DEFAULT NULL COMMENT '发布教师ID',
    total_score INT DEFAULT 100 COMMENT '试卷总分',
    status TINYINT DEFAULT 1 COMMENT '状态：0草稿 1发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_course_id (course_id),
    KEY idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测评定义表';

CREATE TABLE IF NOT EXISTS exam_question (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    exam_id INT NOT NULL COMMENT '测评ID',
    question_id INT NOT NULL COMMENT '题目ID',
    sort_order INT DEFAULT 1 COMMENT '题目顺序',
    UNIQUE KEY uk_exam_question (exam_id, question_id),
    KEY idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测评题目关联表';

SET @exam_id_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exam_record'
      AND COLUMN_NAME = 'exam_id'
);
SET @add_exam_id_sql = IF(
    @exam_id_column_exists = 0,
    'ALTER TABLE exam_record ADD COLUMN exam_id INT DEFAULT NULL COMMENT ''测评ID'' AFTER user_id',
    'DO 0'
);
PREPARE stmt FROM @add_exam_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO exam (id, exam_name, course_id, creator_id, total_score, status, create_time) VALUES
(1,'初中代数基础阶段测评',1,2,100,1,'2026-07-02 22:10:00'),
(2,'运动和相互作用诊断测评',20,52,100,1,'2026-07-02 22:50:00'),
(3,'数据与算法实践阶段测评',22,53,100,1,'2026-07-02 22:55:00')
ON DUPLICATE KEY UPDATE
exam_name = VALUES(exam_name),
course_id = VALUES(course_id),
creator_id = VALUES(creator_id),
total_score = VALUES(total_score),
status = VALUES(status),
create_time = VALUES(create_time);

INSERT IGNORE INTO exam_question (id, exam_id, question_id, sort_order) VALUES
(1,1,1,1),(2,1,2,2),(3,1,3,3),
(4,2,88,1),(5,2,89,2),(6,2,90,3),(7,2,91,4),(8,2,92,5),
(9,3,104,1),(10,3,105,2),(11,3,106,3),(12,3,107,4),(13,3,108,5);

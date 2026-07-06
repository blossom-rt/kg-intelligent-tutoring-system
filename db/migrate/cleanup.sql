-- ============================================================
-- 数据清理脚本
-- 用途：仅清空演示数据（保留表结构和账号），用于重复导入前重置
-- 警告：会删除除 sys_role 和 sys_user 主键 ID <= 5（admin）以外的所有业务数据！
-- ============================================================

USE kg_tutoring_db;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE ai_call_log;
TRUNCATE TABLE sys_oper_log;
TRUNCATE TABLE sys_notice;
TRUNCATE TABLE wrong_question;
TRUNCATE TABLE exam_record;
TRUNCATE TABLE exam_question;
TRUNCATE TABLE exam;
TRUNCATE TABLE study_record;
TRUNCATE TABLE path_detail;
TRUNCATE TABLE study_path;
TRUNCATE TABLE question;
TRUNCATE TABLE knowledge_edge;
TRUNCATE TABLE cross_theme_node;
TRUNCATE TABLE cross_subject_theme;
TRUNCATE TABLE knowledge_node;
TRUNCATE TABLE course;
TRUNCATE TABLE sys_email_code;

-- 删除测试用户（保留 admin/teacher/student 三个主账号）
DELETE FROM sys_user WHERE id > 5;

SET FOREIGN_KEY_CHECKS = 1;

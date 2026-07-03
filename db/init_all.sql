CREATE DATABASE IF NOT EXISTS kg_tutoring_db
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE kg_tutoring_db;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS sys_role (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色主键ID',
    role_code VARCHAR(30) NOT NULL UNIQUE COMMENT '角色唯一标识：admin/teacher/student',
    role_name VARCHAR(30) NOT NULL COMMENT '角色显示名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述与权限范围'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_user (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    real_name VARCHAR(30) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(50) DEFAULT NULL COMMENT '绑定邮箱',
    role_id INT NOT NULL COMMENT '关联角色ID',
    status TINYINT DEFAULT 1 COMMENT '账号状态：1正常 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    UNIQUE KEY uk_email (email),
    KEY idx_role_id (role_id),
    CONSTRAINT fk_sys_user_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_email_code (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    email VARCHAR(50) NOT NULL COMMENT '目标邮箱',
    code VARCHAR(10) NOT NULL COMMENT '6位验证码',
    type VARCHAR(20) NOT NULL COMMENT '类型：register注册 reset找回密码',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    is_used TINYINT DEFAULT 0 COMMENT '是否已使用：0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_email_type (email, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮箱验证码表';

CREATE TABLE IF NOT EXISTS course (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '课程主键ID',
    subject VARCHAR(30) NOT NULL COMMENT '所属学科',
    course_name VARCHAR(50) NOT NULL COMMENT '课程名称',
    description TEXT DEFAULT NULL COMMENT '课程简介',
    teacher_id INT DEFAULT NULL COMMENT '负责教师ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_teacher_id (teacher_id),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表';

CREATE TABLE IF NOT EXISTS knowledge_node (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '知识点主键ID',
    course_id INT NOT NULL COMMENT '所属课程ID',
    name VARCHAR(50) NOT NULL COMMENT '知识点名称',
    description TEXT DEFAULT NULL COMMENT '知识点讲解内容',
    difficulty TINYINT DEFAULT 1 COMMENT '难度：1基础 2进阶 3困难',
    chapter VARCHAR(50) DEFAULT NULL COMMENT '所属章节',
    expected_minutes INT DEFAULT 30 COMMENT '预计学习时长(分钟)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_course_id (course_id),
    CONSTRAINT fk_node_course FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点节点表';

CREATE TABLE IF NOT EXISTS knowledge_edge (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '边主键ID',
    from_node_id INT NOT NULL COMMENT '前置知识点ID',
    to_node_id INT NOT NULL COMMENT '后继知识点ID',
    relation_type VARCHAR(20) DEFAULT 'prerequisite' COMMENT '关系类型',
    is_cross_subject TINYINT DEFAULT 0 COMMENT '是否跨学科：0否 1是',
    UNIQUE KEY uk_from_to (from_node_id, to_node_id),
    KEY idx_to_node_id (to_node_id),
    CONSTRAINT fk_edge_from_node FOREIGN KEY (from_node_id) REFERENCES knowledge_node(id),
    CONSTRAINT fk_edge_to_node FOREIGN KEY (to_node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点依赖边表';

CREATE TABLE IF NOT EXISTS question (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '题目主键ID',
    node_id INT NOT NULL COMMENT '关联知识点ID',
    content TEXT NOT NULL COMMENT '题干',
    options TEXT DEFAULT NULL COMMENT '选项JSON格式',
    answer VARCHAR(200) NOT NULL COMMENT '标准答案',
    analysis TEXT DEFAULT NULL COMMENT '题目解析',
    difficulty TINYINT DEFAULT 1 COMMENT '难度等级',
    question_type VARCHAR(20) DEFAULT 'single' COMMENT '题型：single/multi/judge',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_node_id (node_id),
    CONSTRAINT fk_question_node FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='习题库表';

CREATE TABLE IF NOT EXISTS study_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录主键ID',
    user_id INT NOT NULL COMMENT '学生ID',
    node_id INT NOT NULL COMMENT '知识点ID',
    mastery_level TINYINT DEFAULT 0 COMMENT '掌握度：0未学 1学习中 2已掌握',
    correct_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '答题正确率',
    study_minutes INT DEFAULT 0 COMMENT '累计学习时长(分钟)',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_node (user_id, node_id),
    KEY idx_node_id (node_id),
    CONSTRAINT fk_study_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_study_record_node FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点学习记录表';

CREATE TABLE IF NOT EXISTS study_path (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '路径主键ID',
    user_id INT NOT NULL COMMENT '学生ID',
    target_node_id INT NOT NULL COMMENT '目标知识点ID',
    path_name VARCHAR(100) DEFAULT NULL COMMENT '路径名称',
    total_nodes INT DEFAULT NULL COMMENT '总节点数',
    total_minutes INT DEFAULT NULL COMMENT '总预计时长(分钟)',
    ai_summary TEXT DEFAULT NULL COMMENT '路径总览',
    status TINYINT DEFAULT 0 COMMENT '状态：0进行中 1已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_target_node_id (target_node_id),
    CONSTRAINT fk_study_path_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_study_path_target_node FOREIGN KEY (target_node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习路径主表';

CREATE TABLE IF NOT EXISTS path_detail (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '详情主键ID',
    path_id INT NOT NULL COMMENT '所属路径ID',
    node_id INT NOT NULL COMMENT '知识点ID',
    sort_order INT NOT NULL COMMENT '学习顺序号',
    is_finished TINYINT DEFAULT 0 COMMENT '是否完成：0否 1是',
    KEY idx_path_id (path_id),
    KEY idx_node_id (node_id),
    CONSTRAINT fk_path_detail_path FOREIGN KEY (path_id) REFERENCES study_path(id),
    CONSTRAINT fk_path_detail_node FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习路径详情表';

CREATE TABLE IF NOT EXISTS exam (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '测评ID',
    exam_name VARCHAR(100) NOT NULL COMMENT '测评名称',
    course_id INT NOT NULL COMMENT '所属课程ID',
    creator_id INT DEFAULT NULL COMMENT '发布教师ID',
    total_score INT DEFAULT 100 COMMENT '试卷总分',
    status TINYINT DEFAULT 1 COMMENT '状态：0草稿 1发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_course_id (course_id),
    KEY idx_creator_id (creator_id),
    CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES course(id),
    CONSTRAINT fk_exam_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测评定义表';

CREATE TABLE IF NOT EXISTS exam_question (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    exam_id INT NOT NULL COMMENT '测评ID',
    question_id INT NOT NULL COMMENT '题目ID',
    sort_order INT DEFAULT 1 COMMENT '题目顺序',
    UNIQUE KEY uk_exam_question (exam_id, question_id),
    KEY idx_question_id (question_id),
    CONSTRAINT fk_exam_question_exam FOREIGN KEY (exam_id) REFERENCES exam(id),
    CONSTRAINT fk_exam_question_question FOREIGN KEY (question_id) REFERENCES question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测评题目关联表';

CREATE TABLE IF NOT EXISTS exam_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '测评记录ID',
    user_id INT NOT NULL COMMENT '学生ID',
    exam_id INT DEFAULT NULL COMMENT '测评ID',
    course_id INT DEFAULT NULL COMMENT '所属课程ID',
    total_score INT DEFAULT NULL COMMENT '试卷总分',
    user_score DECIMAL(5,2) DEFAULT NULL COMMENT '学生得分',
    ai_report TEXT DEFAULT NULL COMMENT '诊断报告',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
    KEY idx_user_id (user_id),
    KEY idx_exam_id (exam_id),
    KEY idx_course_id (course_id),
    CONSTRAINT fk_exam_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_exam_record_exam FOREIGN KEY (exam_id) REFERENCES exam(id),
    CONSTRAINT fk_exam_record_course FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测评记录表';

CREATE TABLE IF NOT EXISTS wrong_question (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '错题ID',
    user_id INT NOT NULL COMMENT '学生ID',
    question_id INT NOT NULL COMMENT '题目ID',
    wrong_answer VARCHAR(200) DEFAULT NULL COMMENT '错误答案',
    wrong_count INT DEFAULT 1 COMMENT '错误次数',
    ai_explain TEXT DEFAULT NULL COMMENT '错题讲解',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次错误时间',
    UNIQUE KEY uk_user_question (user_id, question_id),
    KEY idx_question_id (question_id),
    CONSTRAINT fk_wrong_question_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_wrong_question_question FOREIGN KEY (question_id) REFERENCES question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题本表';

CREATE TABLE IF NOT EXISTS cross_subject_theme (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主题主键ID',
    theme_name VARCHAR(100) NOT NULL COMMENT '主题名称',
    description TEXT DEFAULT NULL COMMENT '主题背景与目标',
    difficulty TINYINT DEFAULT 2 COMMENT '主题难度',
    publisher_id INT DEFAULT NULL COMMENT '发布教师ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0下架 1发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_publisher_id (publisher_id),
    CONSTRAINT fk_theme_publisher FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨学科主题表';

CREATE TABLE IF NOT EXISTS cross_theme_node (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录ID',
    theme_id INT NOT NULL COMMENT '主题ID',
    node_id INT NOT NULL COMMENT '知识点ID',
    UNIQUE KEY uk_theme_node (theme_id, node_id),
    KEY idx_node_id (node_id),
    CONSTRAINT fk_cross_theme FOREIGN KEY (theme_id) REFERENCES cross_subject_theme(id),
    CONSTRAINT fk_cross_node FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主题-知识点关联表';

CREATE TABLE IF NOT EXISTS sys_notice (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(100) NOT NULL COMMENT '公告标题',
    content TEXT DEFAULT NULL COMMENT '公告正文',
    publisher_id INT DEFAULT NULL COMMENT '发布人ID',
    target_role VARCHAR(20) DEFAULT 'all' COMMENT '推送对象：all/student/teacher',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    KEY idx_publisher_id (publisher_id),
    CONSTRAINT fk_notice_publisher FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';

CREATE TABLE IF NOT EXISTS sys_oper_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id INT DEFAULT NULL COMMENT '操作人ID',
    module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    operation VARCHAR(200) DEFAULT NULL COMMENT '操作内容',
    ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统操作日志表';

CREATE TABLE IF NOT EXISTS ai_call_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id INT DEFAULT NULL COMMENT '调用用户ID',
    scene VARCHAR(50) DEFAULT NULL COMMENT '调用场景',
    prompt TEXT DEFAULT NULL COMMENT '输入提示词',
    result TEXT DEFAULT NULL COMMENT '返回结果',
    call_duration INT DEFAULT NULL COMMENT '调用耗时ms',
    status TINYINT DEFAULT 1 COMMENT '状态：1成功 0失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    INDEX idx_user_id (user_id),
    INDEX idx_scene (scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调用日志表';

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_role (role_code, role_name, description) VALUES
('admin', '系统管理员', '系统最高权限，负责运维与用户管理'),
('teacher', '教师', '负责课程、知识点、题库管理与学情跟踪'),
('student', '学生', '自主学习、刷题测评、获取个性化路径')
ON DUPLICATE KEY UPDATE
role_name = VALUES(role_name),
description = VALUES(description);

INSERT INTO sys_user (id, username, password, real_name, email, role_id, status)
SELECT 5, 'admin', MD5('admin123'), '系统管理员', 'admin@test.com', id, 1
FROM sys_role
WHERE role_code = 'admin'
  AND NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

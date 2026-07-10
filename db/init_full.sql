-- ============================================================
-- 第 1 部分：建表 + 基础角色/管理员（来自 init_all.sql）
-- ============================================================

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
    node_type VARCHAR(20) DEFAULT 'concept' COMMENT '节点类型：concept概念 skill技能 application应用',
    learning_goal VARCHAR(200) DEFAULT NULL COMMENT '学习目标',
    keywords VARCHAR(200) DEFAULT NULL COMMENT '关键词，逗号分隔',
    example_hint VARCHAR(300) DEFAULT NULL COMMENT '例题或学习提示',
    difficulty TINYINT DEFAULT 1 COMMENT '难度：1基础 2进阶 3困难',
    chapter VARCHAR(50) DEFAULT NULL COMMENT '所属章节',
    expected_minutes INT DEFAULT 30 COMMENT '预计学习时长(分钟)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_course_id (course_id),
    CONSTRAINT fk_node_course FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点节点表';

CREATE TABLE IF NOT EXISTS learning_resource (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '学习资源主键ID',
    node_id INT NOT NULL COMMENT '关联知识点ID',
    title VARCHAR(120) NOT NULL COMMENT '资源标题',
    resource_type VARCHAR(20) DEFAULT 'video' COMMENT '资源类型：video/article/pdf/link',
    url VARCHAR(500) NOT NULL COMMENT '资源地址',
    cover_url VARCHAR(500) DEFAULT NULL COMMENT '封面地址',
    duration_seconds INT DEFAULT NULL COMMENT '资源时长(秒)',
    source VARCHAR(50) DEFAULT 'custom' COMMENT '资源来源：bilibili/mooc/local/custom',
    sort_order INT DEFAULT 0 COMMENT '排序值',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_resource_node_id (node_id),
    CONSTRAINT fk_resource_node FOREIGN KEY (node_id) REFERENCES knowledge_node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点学习资源表';

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

-- ============================================================
-- 第 2 部分：演示数据（来自 seed_all.sql，已去除 dump 头尾）
-- ============================================================

-- Demo data import for kg_tutoring_db.
-- Run after init_all.sql:
--   mysql -u root -p < seed_all.sql

USE kg_tutoring_db;
SET FOREIGN_KEY_CHECKS = 0;


--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT  IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`) VALUES (1,'admin','系统管理员','系统最高权限，负责运维与用户管理'),(2,'teacher','教师','负责课程、知识点、题库管理与学情跟踪'),(3,'student','学生','自主学习、刷题测评、获取个性化路径');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT  IGNORE INTO `sys_user` (`id`, `username`, `real_name`, `email`, `role_id`, `status`, `create_time`, `password`) VALUES (1,'student','张小明','student@test.com',3,1,'2026-07-02 19:26:08','e10adc3949ba59abbe56e057f20f883e'),(2,'teacher','李老师','teacher@test.com',2,1,'2026-07-02 19:26:08','e10adc3949ba59abbe56e057f20f883e'),(5,'admin','系统管理员','admin@test.com',1,1,'2026-07-02 19:26:12','0192023a7bbd73250516f069df18b500'),(51,'teacher_math','王数理','teacher_math@test.com',2,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(52,'teacher_science','陈科学','teacher_science@test.com',2,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(53,'teacher_it','赵信息','teacher_it@test.com',2,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(54,'student01','林一航','student01@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(55,'student02','周子涵','student02@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(56,'student03','吴思远','student03@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(57,'student04','郑雨桐','student04@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(58,'student05','何嘉宁','student05@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(59,'student06','许明哲','student06@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(60,'student07','孙若溪','student07@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(61,'student08','高晨曦','student08@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(62,'student09','罗景行','student09@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(63,'student10','唐语嫣','student10@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(64,'student11','马知行','student11@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e'),(65,'student12','梁安然','student12@test.com',3,1,'2026-07-02 22:47:16','e10adc3949ba59abbe56e057f20f883e');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sys_email_code`
--

LOCK TABLES `sys_email_code` WRITE;
/*!40000 ALTER TABLE `sys_email_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_email_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT  IGNORE INTO `course` (`id`, `subject`, `course_name`, `description`, `teacher_id`, `create_time`) VALUES (1,'数学','初中代数基础','覆盖有理数、一次方程、函数入门等核心知识点。',2,'2026-07-02 22:02:07'),(2,'物理','力学入门','从速度、力、压强到简单机械，建立基础物理模型。',2,'2026-07-02 22:02:07'),(3,'信息技术','Python程序设计','学习变量、条件、循环和函数，完成小型程序设计任务。',2,'2026-07-02 22:02:07'),(18,'数学','数与代数综合','围绕数、式、方程、不等式和函数建立代数推理能力。',51,'2026-07-02 22:47:16'),(19,'数学','几何与统计入门','覆盖图形认识、三角形、四边形、统计图表和概率初步。',51,'2026-07-02 22:47:16'),(20,'物理','运动和相互作用','围绕机械运动、力、压强、浮力和简单机械建立物理模型。',52,'2026-07-02 22:47:16'),(21,'物理','能量与电学基础','学习功、功率、机械能、内能、电路和欧姆定律等基础内容。',52,'2026-07-02 22:47:16'),(22,'信息科技','数据与算法实践','围绕数据、算法、程序控制结构和问题解决展开项目实践。',53,'2026-07-02 22:47:16'),(23,'跨学科','校园数据探究项目','综合数学、物理和信息科技，完成校园真实情境的数据建模。',53,'2026-07-02 22:47:16');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `knowledge_node`
--

LOCK TABLES `knowledge_node` WRITE;
/*!40000 ALTER TABLE `knowledge_node` DISABLE KEYS */;
INSERT  IGNORE INTO `knowledge_node` (`id`, `course_id`, `name`, `description`, `difficulty`, `chapter`, `expected_minutes`, `create_time`) VALUES (1,1,'有理数运算','掌握正负数、绝对值以及有理数四则运算。',1,'第一章 有理数',30,'2026-07-02 22:02:07'),(2,1,'一元一次方程','理解方程思想，能够列方程解决实际问题。',2,'第二章 方程',40,'2026-07-02 22:02:07'),(3,1,'一次函数','理解函数表达式、图像和变化趋势。',3,'第三章 函数',50,'2026-07-02 22:02:07'),(4,2,'速度与路程','理解速度、路程、时间三者关系。',1,'第一章 运动',30,'2026-07-02 22:02:07'),(5,2,'力的合成','学习同一直线力的合成与受力分析。',2,'第二章 力',45,'2026-07-02 22:02:07'),(6,3,'变量与表达式','理解变量、数据类型和基础表达式。',1,'第一章 Python基础',25,'2026-07-02 22:02:07'),(7,3,'条件分支','使用 if/else 表达选择逻辑。',2,'第二章 控制结构',35,'2026-07-02 22:02:07'),(8,3,'循环结构','使用 for 和 while 处理重复任务。',2,'第二章 控制结构',40,'2026-07-02 22:02:07'),(135,18,'整数与有理数','理解数轴、相反数、绝对值和有理数运算。',1,'数的认识',30,'2026-07-02 22:47:16'),(136,18,'整式加减','理解同类项、去括号和整式化简。',1,'式的运算',35,'2026-07-02 22:47:16'),(137,18,'一元一次方程应用','用方程表示数量关系并解决行程、工程、销售问题。',2,'方程模型',45,'2026-07-02 22:47:16'),(138,18,'不等式与不等式组','理解不等关系并会解简单不等式组。',2,'不等关系',45,'2026-07-02 22:47:16'),(139,18,'平面直角坐标系','用坐标描述点的位置和图形变化。',2,'坐标与图形',40,'2026-07-02 22:47:16'),(140,18,'一次函数模型','理解函数图像、斜率和实际变化关系。',3,'函数入门',55,'2026-07-02 22:47:16'),(141,18,'二元一次方程组','用代入法、加减法解决两个未知量问题。',3,'方程组',50,'2026-07-02 22:47:16'),(142,18,'数据的集中趋势','会计算平均数、中位数、众数并解释数据含义。',2,'统计初步',35,'2026-07-02 22:47:16'),(143,19,'线段角与相交线','认识线段、角、平行线和垂直关系。',1,'图形基础',30,'2026-07-02 22:47:16'),(144,19,'三角形性质','理解三角形内角和、边角关系和稳定性。',2,'三角形',40,'2026-07-02 22:47:16'),(145,19,'全等三角形','掌握全等判定并用于证明。',3,'几何证明',55,'2026-07-02 22:47:16'),(146,19,'轴对称与平移','理解图形变换和坐标中的平移规律。',2,'图形变换',40,'2026-07-02 22:47:16'),(147,19,'四边形基础','认识平行四边形、矩形、菱形和正方形性质。',2,'四边形',45,'2026-07-02 22:47:16'),(148,19,'圆的初步认识','理解圆心、半径、直径和圆周角等基本概念。',3,'圆',50,'2026-07-02 22:47:16'),(149,19,'统计图表读取','能读取条形图、折线图和扇形图。',1,'统计图表',30,'2026-07-02 22:47:16'),(150,19,'概率初步','用频率估计概率，理解随机事件。',2,'概率',35,'2026-07-02 22:47:16'),(151,20,'机械运动描述','用参照物、路程、时间和速度描述运动。',1,'运动',30,'2026-07-02 22:47:16'),(152,20,'速度图像','通过图像分析匀速直线运动。',2,'运动图像',40,'2026-07-02 22:47:16'),(153,20,'质量与密度','理解质量、密度及其测量。',2,'物质属性',40,'2026-07-02 22:47:16'),(154,20,'力和弹力','认识力的作用效果、三要素和弹力。',1,'力',35,'2026-07-02 22:47:16'),(155,20,'重力与摩擦力','分析重力、摩擦力在生活中的作用。',2,'常见力',45,'2026-07-02 22:47:16'),(156,20,'二力平衡','理解平衡状态和二力平衡条件。',3,'力与运动',50,'2026-07-02 22:47:16'),(157,20,'压强','理解压力、压强公式和液体压强。',3,'压强',55,'2026-07-02 22:47:16'),(158,20,'浮力','理解阿基米德原理和浮沉条件。',3,'浮力',55,'2026-07-02 22:47:16'),(159,21,'功和功率','理解做功条件、功率和机械效率。',2,'机械能',45,'2026-07-02 22:47:16'),(160,21,'动能和势能','理解能量转化与守恒的初步思想。',2,'机械能',45,'2026-07-02 22:47:16'),(161,21,'内能与热量','认识温度、内能、热传递和比热容。',2,'热学',45,'2026-07-02 22:47:16'),(162,21,'简单电路','认识电源、用电器、开关和电路图。',1,'电路',35,'2026-07-02 22:47:16'),(163,21,'电流和电压','理解电流、电压及测量方法。',2,'电学量',45,'2026-07-02 22:47:16'),(164,21,'电阻','理解电阻与材料、长度、横截面积的关系。',2,'电学量',40,'2026-07-02 22:47:16'),(165,21,'欧姆定律','应用 I=U/R 解决简单电路问题。',3,'电路规律',55,'2026-07-02 22:47:16'),(166,21,'电功率','理解电功、电功率和安全用电。',3,'电能',55,'2026-07-02 22:47:16'),(167,22,'数据采集','理解数据来源、采样和数据质量。',1,'数据',30,'2026-07-02 22:47:16'),(168,22,'数据整理','会使用表格组织、清洗和筛选数据。',1,'数据处理',35,'2026-07-02 22:47:16'),(169,22,'流程图','用流程图表达算法步骤和分支结构。',1,'算法表达',30,'2026-07-02 22:47:16'),(170,22,'变量和表达式','理解变量、数据类型和表达式求值。',1,'程序基础',30,'2026-07-02 22:47:16'),(171,22,'条件结构','用条件语句处理分支决策。',2,'程序控制',40,'2026-07-02 22:47:16'),(172,22,'循环结构','用循环处理重复计算和批量数据。',2,'程序控制',45,'2026-07-02 22:47:16'),(173,22,'列表数据处理','使用列表保存和遍历多项数据。',2,'数据结构',45,'2026-07-02 22:47:16'),(174,22,'简单排序算法','理解选择排序、冒泡排序的基本思想。',3,'算法实践',55,'2026-07-02 22:47:16'),(175,23,'提出研究问题','从校园情境中提出可用数据回答的问题。',1,'项目启动',25,'2026-07-02 22:47:16'),(176,23,'设计调查表','设计变量、选项和采样方式。',1,'数据采集',30,'2026-07-02 22:47:16'),(177,23,'数据可视化','用图表展示趋势、比例和比较关系。',2,'数据表达',40,'2026-07-02 22:47:16'),(178,23,'建立数学模型','选择合适公式或函数描述数据关系。',3,'模型建立',55,'2026-07-02 22:47:16'),(179,23,'物理量测量','测量路程、时间、质量等实验数据。',2,'实验测量',45,'2026-07-02 22:47:16'),(180,23,'程序化统计','用程序计算平均值、最大值和排序。',2,'程序分析',45,'2026-07-02 22:47:16'),(181,23,'解释与评价','结合证据解释结论并评估误差。',3,'结论表达',50,'2026-07-02 22:47:16'),(182,23,'项目展示','制作报告并进行口头展示。',2,'成果展示',40,'2026-07-02 22:47:16');
/*!40000 ALTER TABLE `knowledge_node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `knowledge_edge`
--

LOCK TABLES `knowledge_edge` WRITE;
/*!40000 ALTER TABLE `knowledge_edge` DISABLE KEYS */;
INSERT  IGNORE INTO `knowledge_edge` (`id`, `from_node_id`, `to_node_id`, `relation_type`, `is_cross_subject`) VALUES (1,1,2,'prerequisite',0),(2,2,3,'prerequisite',0),(3,1,4,'support',1),(4,4,5,'prerequisite',0),(5,6,7,'prerequisite',0),(6,7,8,'prerequisite',0),(7,2,6,'support',1),(78,135,136,'prerequisite',0),(79,136,137,'prerequisite',0),(80,137,138,'prerequisite',0),(81,138,139,'prerequisite',0),(82,139,140,'prerequisite',0),(83,140,141,'prerequisite',0),(84,141,142,'prerequisite',0),(85,143,144,'prerequisite',0),(86,144,145,'prerequisite',0),(87,145,146,'prerequisite',0),(88,146,147,'prerequisite',0),(89,147,148,'prerequisite',0),(90,148,149,'prerequisite',0),(91,149,150,'prerequisite',0),(92,151,152,'prerequisite',0),(93,152,153,'prerequisite',0),(94,153,154,'prerequisite',0),(95,154,155,'prerequisite',0),(96,155,156,'prerequisite',0),(97,156,157,'prerequisite',0),(98,157,158,'prerequisite',0),(99,159,160,'prerequisite',0),(100,160,161,'prerequisite',0),(101,161,162,'prerequisite',0),(102,162,163,'prerequisite',0),(103,163,164,'prerequisite',0),(104,164,165,'prerequisite',0),(105,165,166,'prerequisite',0),(106,167,168,'prerequisite',0),(107,168,169,'prerequisite',0),(108,169,170,'prerequisite',0),(109,170,171,'prerequisite',0),(110,171,172,'prerequisite',0),(111,172,173,'prerequisite',0),(112,173,174,'prerequisite',0),(113,175,176,'prerequisite',0),(114,176,177,'prerequisite',0),(115,177,178,'prerequisite',0),(116,178,179,'prerequisite',0),(117,179,180,'prerequisite',0),(118,180,181,'prerequisite',0),(119,181,182,'prerequisite',0),(141,140,152,'support',1),(142,142,168,'support',1),(143,149,177,'support',1),(144,151,179,'support',1),(145,172,180,'support',1),(146,166,181,'support',1);
/*!40000 ALTER TABLE `knowledge_edge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
INSERT  IGNORE INTO `question` (`id`, `node_id`, `content`, `options`, `answer`, `analysis`, `difficulty`, `question_type`, `create_time`) VALUES (1,1,'计算：-3 + 5 = ?','{\"A\":\"-8\",\"B\":\"2\",\"C\":\"8\",\"D\":\"-2\"}','B','异号相加，用较大绝对值减较小绝对值，结果取 5 的正号。',1,'single','2026-07-02 22:02:07'),(2,2,'方程 2x + 3 = 11 的解是？','{\"A\":\"x=2\",\"B\":\"x=3\",\"C\":\"x=4\",\"D\":\"x=7\"}','C','两边先减 3 得 2x=8，再除以 2 得 x=4。',2,'single','2026-07-02 22:02:07'),(3,3,'一次函数 y = 2x + 1 中，斜率是多少？','{\"A\":\"1\",\"B\":\"2\",\"C\":\"x\",\"D\":\"无法确定\"}','B','一次函数 y=kx+b 中，k 是斜率。',2,'single','2026-07-02 22:02:07'),(4,4,'小车 2 小时行驶 60 千米，平均速度是多少？','{\"A\":\"20 km/h\",\"B\":\"30 km/h\",\"C\":\"60 km/h\",\"D\":\"120 km/h\"}','B','速度 = 路程 ÷ 时间 = 60 ÷ 2 = 30 km/h。',1,'single','2026-07-02 22:02:07'),(5,5,'同一直线上方向相同的 3N 和 5N 两个力，合力为？','{\"A\":\"2N\",\"B\":\"3N\",\"C\":\"5N\",\"D\":\"8N\"}','D','方向相同的力合成时大小相加。',2,'single','2026-07-02 22:02:07'),(6,6,'Python 中，下面哪个语句可以给变量 age 赋值 12？','{\"A\":\"age == 12\",\"B\":\"age = 12\",\"C\":\"12 = age\",\"D\":\"int age = 12\"}','B','Python 使用单等号进行赋值。',1,'single','2026-07-02 22:02:07'),(7,7,'判断 x 是否大于 0，Python 条件应写作？','{\"A\":\"if x > 0:\",\"B\":\"if (x > 0) then\",\"C\":\"when x > 0\",\"D\":\"if x > 0\"}','A','Python 的 if 语句以冒号结尾。',2,'single','2026-07-02 22:02:07'),(8,8,'下面哪个关键字常用于遍历列表？','{\"A\":\"for\",\"B\":\"class\",\"C\":\"import\",\"D\":\"return\"}','A','for 循环常用于遍历序列。',2,'single','2026-07-02 22:02:07'),(72,135,'数轴上表示 -2 的点到原点的距离是多少？','{\"A\":\"-2\",\"B\":\"0\",\"C\":\"2\",\"D\":\"4\"}','C','到原点距离表示绝对值，|-2|=2。',1,'single','2026-07-02 22:47:16'),(73,136,'化简 3a+2a 的结果是？','{\"A\":\"5a\",\"B\":\"6a\",\"C\":\"5a²\",\"D\":\"a\"}','A','同类项系数相加，字母部分不变。',1,'single','2026-07-02 22:47:16'),(74,137,'某数的 2 倍加 4 等于 10，这个数是？','{\"A\":\"2\",\"B\":\"3\",\"C\":\"4\",\"D\":\"5\"}','B','设该数为 x，2x+4=10，解得 x=3。',2,'single','2026-07-02 22:47:16'),(75,138,'不等式 x+2>5 的解集是？','{\"A\":\"x>3\",\"B\":\"x<3\",\"C\":\"x>7\",\"D\":\"x<7\"}','A','两边同时减 2，得 x>3。',2,'single','2026-07-02 22:47:16'),(76,139,'点 P(3,-2) 位于第几象限？','{\"A\":\"第一象限\",\"B\":\"第二象限\",\"C\":\"第三象限\",\"D\":\"第四象限\"}','D','横坐标为正，纵坐标为负，位于第四象限。',2,'single','2026-07-02 22:47:16'),(77,140,'一次函数 y=3x-1 的斜率是？','{\"A\":\"-1\",\"B\":\"1\",\"C\":\"3\",\"D\":\"x\"}','C','y=kx+b 中 k 是斜率。',2,'single','2026-07-02 22:47:16'),(78,141,'方程组 x+y=5, x-y=1 的解中 x 等于？','{\"A\":\"2\",\"B\":\"3\",\"C\":\"4\",\"D\":\"5\"}','B','两式相加得 2x=6，所以 x=3。',3,'single','2026-07-02 22:47:16'),(79,142,'数据 2,3,3,8 的众数是？','{\"A\":\"2\",\"B\":\"3\",\"C\":\"4\",\"D\":\"8\"}','B','出现次数最多的是 3。',1,'single','2026-07-02 22:47:16'),(80,143,'两条直线相交形成的对顶角关系是？','{\"A\":\"相等\",\"B\":\"互余\",\"C\":\"互补\",\"D\":\"无关系\"}','A','对顶角相等。',1,'single','2026-07-02 22:47:16'),(81,144,'三角形三个内角和是多少？','{\"A\":\"90°\",\"B\":\"120°\",\"C\":\"180°\",\"D\":\"360°\"}','C','任意三角形内角和为 180°。',1,'single','2026-07-02 22:47:16'),(82,145,'SSS 判定指的是哪三组元素相等？','{\"A\":\"三边\",\"B\":\"两边一角\",\"C\":\"三角\",\"D\":\"一边两角\"}','A','SSS 表示三边分别相等。',2,'single','2026-07-02 22:47:16'),(83,146,'平移变换会改变图形的什么？','{\"A\":\"形状\",\"B\":\"大小\",\"C\":\"位置\",\"D\":\"对应边长度\"}','C','平移只改变位置，不改变形状和大小。',1,'single','2026-07-02 22:47:16'),(84,147,'平行四边形的对边关系是？','{\"A\":\"都垂直\",\"B\":\"分别平行且相等\",\"C\":\"都相交\",\"D\":\"都不相等\"}','B','平行四边形对边分别平行且相等。',2,'single','2026-07-02 22:47:16'),(85,148,'圆中连接圆心和圆上一点的线段叫做？','{\"A\":\"直径\",\"B\":\"半径\",\"C\":\"弦\",\"D\":\"切线\"}','B','圆心到圆上一点的线段是半径。',1,'single','2026-07-02 22:47:16'),(86,149,'最适合表示一周气温变化趋势的是？','{\"A\":\"折线图\",\"B\":\"扇形图\",\"C\":\"流程图\",\"D\":\"电路图\"}','A','折线图适合表现随时间变化的趋势。',1,'single','2026-07-02 22:47:16'),(87,150,'抛一枚均匀硬币，正面朝上的概率是？','{\"A\":\"0\",\"B\":\"1/4\",\"C\":\"1/2\",\"D\":\"1\"}','C','均匀硬币正反两种结果等可能。',1,'single','2026-07-02 22:47:16'),(88,151,'判断物体是否运动，需要先选定什么？','{\"A\":\"参照物\",\"B\":\"温度计\",\"C\":\"电流表\",\"D\":\"天平\"}','A','运动和静止具有相对性，要选参照物。',1,'single','2026-07-02 22:47:16'),(89,152,'匀速直线运动的 s-t 图像通常是什么形状？','{\"A\":\"过原点的直线\",\"B\":\"圆\",\"C\":\"抛物线\",\"D\":\"折线必弯曲\"}','A','匀速时路程与时间成正比。',2,'single','2026-07-02 22:47:16'),(90,153,'密度的计算公式是？','{\"A\":\"ρ=m/V\",\"B\":\"ρ=V/m\",\"C\":\"ρ=mg\",\"D\":\"ρ=F/S\"}','A','密度等于质量除以体积。',2,'single','2026-07-02 22:47:16'),(91,154,'力的单位是？','{\"A\":\"千克\",\"B\":\"牛顿\",\"C\":\"米\",\"D\":\"焦耳\"}','B','力的国际单位是牛顿，符号 N。',1,'single','2026-07-02 22:47:16'),(92,155,'重力的方向总是？','{\"A\":\"水平向左\",\"B\":\"垂直向上\",\"C\":\"竖直向下\",\"D\":\"沿运动方向\"}','C','重力方向总是竖直向下。',1,'single','2026-07-02 22:47:16'),(93,156,'二力平衡时两个力必须满足什么条件？','{\"A\":\"大小相等方向相反同一直线\",\"B\":\"大小不同\",\"C\":\"方向相同\",\"D\":\"作用在不同物体上\"}','A','二力平衡要求同物、等大、反向、共线。',3,'single','2026-07-02 22:47:16'),(94,157,'压强公式是？','{\"A\":\"p=F/S\",\"B\":\"p=Fs\",\"C\":\"p=mv\",\"D\":\"p=U/I\"}','A','压强等于压力除以受力面积。',2,'single','2026-07-02 22:47:16'),(95,158,'浸在液体中的物体受到的浮力方向是？','{\"A\":\"竖直向上\",\"B\":\"竖直向下\",\"C\":\"水平向左\",\"D\":\"随机方向\"}','A','液体对物体的浮力方向竖直向上。',2,'single','2026-07-02 22:47:16'),(96,159,'功率表示什么物理意义？','{\"A\":\"做功快慢\",\"B\":\"物体质量\",\"C\":\"电荷多少\",\"D\":\"温度高低\"}','A','功率是单位时间内完成的功。',2,'single','2026-07-02 22:47:16'),(97,160,'运动物体由于运动而具有的能叫做？','{\"A\":\"动能\",\"B\":\"重力势能\",\"C\":\"内能\",\"D\":\"电能\"}','A','动能与物体质量和速度有关。',1,'single','2026-07-02 22:47:16'),(98,161,'热传递过程中传递的能量叫做？','{\"A\":\"热量\",\"B\":\"质量\",\"C\":\"电流\",\"D\":\"压力\"}','A','热量是热传递过程中转移的能量。',1,'single','2026-07-02 22:47:16'),(99,162,'控制电路通断的元件是？','{\"A\":\"开关\",\"B\":\"电源\",\"C\":\"导线\",\"D\":\"电流表\"}','A','开关用于控制电路通断。',1,'single','2026-07-02 22:47:16'),(100,163,'电压的单位是？','{\"A\":\"安培\",\"B\":\"伏特\",\"C\":\"欧姆\",\"D\":\"瓦特\"}','B','电压单位是伏特，符号 V。',1,'single','2026-07-02 22:47:16'),(101,164,'电阻的单位是？','{\"A\":\"欧姆\",\"B\":\"焦耳\",\"C\":\"牛顿\",\"D\":\"米\"}','A','电阻单位是欧姆，符号 Ω。',1,'single','2026-07-02 22:47:16'),(102,165,'欧姆定律的表达式是？','{\"A\":\"I=U/R\",\"B\":\"P=F/S\",\"C\":\"v=s+t\",\"D\":\"Q=cm\"}','A','通过导体的电流与电压成正比、与电阻成反比。',3,'single','2026-07-02 22:47:16'),(103,166,'电功率 P 与电压 U、电流 I 的关系是？','{\"A\":\"P=UI\",\"B\":\"P=U+I\",\"C\":\"P=U/I\",\"D\":\"P=I/U\"}','A','电功率常用公式 P=UI。',2,'single','2026-07-02 22:47:16'),(104,167,'问卷调查前最需要明确的是？','{\"A\":\"研究问题\",\"B\":\"字体颜色\",\"C\":\"文件名\",\"D\":\"电脑品牌\"}','A','数据采集应服务于明确的研究问题。',1,'single','2026-07-02 22:47:16'),(105,168,'删除明显重复记录属于哪类操作？','{\"A\":\"数据清洗\",\"B\":\"编译程序\",\"C\":\"画电路图\",\"D\":\"几何证明\"}','A','去重是常见的数据清洗任务。',1,'single','2026-07-02 22:47:16'),(106,169,'流程图中的菱形通常表示什么？','{\"A\":\"判断\",\"B\":\"开始结束\",\"C\":\"输入输出\",\"D\":\"普通处理\"}','A','菱形常用于表示条件判断。',1,'single','2026-07-02 22:47:16'),(107,170,'Python 中 a=5 表示什么？','{\"A\":\"给变量 a 赋值 5\",\"B\":\"判断 a 等于 5\",\"C\":\"输出 a\",\"D\":\"删除 a\"}','A','单等号用于赋值。',1,'single','2026-07-02 22:47:16'),(108,171,'Python 的 if 语句行末通常需要什么符号？','{\"A\":\":\",\"B\":\";\",\"C\":\",\",\"D\":\".\"}','A','Python 条件语句块前以冒号结尾。',1,'single','2026-07-02 22:47:16'),(109,172,'处理 100 个同类数据时通常适合使用？','{\"A\":\"循环\",\"B\":\"只写一次 print\",\"C\":\"删除变量\",\"D\":\"手动画图\"}','A','循环适合处理重复任务。',1,'single','2026-07-02 22:47:16'),(110,173,'Python 列表通常用什么符号表示？','{\"A\":\"[]\",\"B\":\"{}\",\"C\":\"()\",\"D\":\"<>\"}','A','列表字面量使用方括号。',1,'single','2026-07-02 22:47:16'),(111,174,'冒泡排序每一轮常把较大的元素逐步移动到哪里？','{\"A\":\"末尾\",\"B\":\"开头\",\"C\":\"随机位置\",\"D\":\"删除\"}','A','冒泡排序通过相邻比较交换，较大值逐步到末尾。',3,'single','2026-07-02 22:47:16'),(112,175,'“课间运动量是否影响心率”属于什么？','{\"A\":\"研究问题\",\"B\":\"电路元件\",\"C\":\"几何公理\",\"D\":\"函数图像\"}','A','它提出了可通过数据探究的问题。',1,'single','2026-07-02 22:47:16'),(113,176,'调查表中“每天运动分钟数”属于什么？','{\"A\":\"变量\",\"B\":\"答案解析\",\"C\":\"电阻\",\"D\":\"直线\"}','A','变量是调查中记录和分析的对象。',1,'single','2026-07-02 22:47:16'),(114,177,'比较不同班级平均运动时间适合用什么图？','{\"A\":\"柱状图\",\"B\":\"电路图\",\"C\":\"地图\",\"D\":\"几何作图\"}','A','柱状图适合比较不同类别的数据。',1,'single','2026-07-02 22:47:16'),(115,178,'用 y=2x+1 描述两个量关系属于什么？','{\"A\":\"数学模型\",\"B\":\"物理仪器\",\"C\":\"调查选项\",\"D\":\"网页标题\"}','A','函数表达式可以作为数量关系模型。',2,'single','2026-07-02 22:47:16'),(116,179,'测量跑步用时通常使用什么工具？','{\"A\":\"秒表\",\"B\":\"量筒\",\"C\":\"电压表\",\"D\":\"弹簧测力计\"}','A','秒表用于测量时间。',1,'single','2026-07-02 22:47:16'),(117,180,'计算列表 nums 的元素个数可使用？','{\"A\":\"len(nums)\",\"B\":\"print = nums\",\"C\":\"if nums\",\"D\":\"nums()len\"}','A','len 函数返回序列长度。',2,'single','2026-07-02 22:47:16'),(118,181,'发现异常值后，合理做法是？','{\"A\":\"核查数据来源\",\"B\":\"直接删除所有数据\",\"C\":\"忽略研究问题\",\"D\":\"停止记录\"}','A','异常值需要结合来源和情境判断。',2,'single','2026-07-02 22:47:16'),(119,182,'展示项目结论时最重要的是？','{\"A\":\"证据支持\",\"B\":\"字体越多越好\",\"C\":\"只展示封面\",\"D\":\"不说明方法\"}','A','结论应由数据和分析证据支撑。',1,'single','2026-07-02 22:47:16');
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `study_record`
--

LOCK TABLES `study_record` WRITE;
/*!40000 ALTER TABLE `study_record` DISABLE KEYS */;
INSERT  IGNORE INTO `study_record` (`id`, `user_id`, `node_id`, `mastery_level`, `correct_rate`, `study_minutes`, `update_time`) VALUES (1,1,1,2,92.00,45,'2026-07-02 22:02:07'),(2,1,2,1,68.00,30,'2026-07-02 22:02:07'),(3,1,3,0,0.00,0,'2026-07-02 22:02:07'),(4,1,4,2,85.00,35,'2026-07-02 22:02:07'),(5,1,6,2,95.00,28,'2026-07-02 22:02:07'),(6,1,7,1,70.00,20,'2026-07-02 22:02:07'),(518,65,135,2,92.00,80,'2026-07-02 22:47:16'),(519,64,135,2,91.00,79,'2026-07-02 22:47:16'),(520,63,135,0,34.00,78,'2026-07-02 22:47:16'),(521,62,135,1,68.00,77,'2026-07-02 22:47:16'),(522,61,135,1,67.00,76,'2026-07-02 22:47:16'),(523,60,135,2,87.00,75,'2026-07-02 22:47:16'),(524,59,135,2,86.00,74,'2026-07-02 22:47:16'),(525,58,135,0,29.00,73,'2026-07-02 22:47:16'),(526,57,135,1,63.00,72,'2026-07-02 22:47:16'),(527,56,135,1,62.00,71,'2026-07-02 22:47:16'),(528,55,135,2,82.00,70,'2026-07-02 22:47:16'),(529,54,135,2,95.00,69,'2026-07-02 22:47:16'),(530,1,135,1,57.00,16,'2026-07-02 22:47:16'),(531,65,136,1,72.00,70,'2026-07-02 22:47:16'),(532,64,136,2,92.00,68,'2026-07-02 22:47:16'),(533,63,136,2,91.00,66,'2026-07-02 22:47:16'),(534,62,136,0,34.00,64,'2026-07-02 22:47:16'),(535,61,136,1,68.00,62,'2026-07-02 22:47:16'),(536,60,136,1,67.00,60,'2026-07-02 22:47:16'),(537,59,136,2,87.00,58,'2026-07-02 22:47:16'),(538,58,136,2,86.00,56,'2026-07-02 22:47:16'),(539,57,136,0,29.00,54,'2026-07-02 22:47:16'),(540,56,136,1,63.00,52,'2026-07-02 22:47:16'),(541,55,136,1,62.00,50,'2026-07-02 22:47:16'),(542,54,136,2,82.00,48,'2026-07-02 22:47:16'),(543,1,136,1,58.00,17,'2026-07-02 22:47:16'),(544,65,137,1,73.00,60,'2026-07-02 22:47:16'),(545,64,137,1,72.00,57,'2026-07-02 22:47:16'),(546,63,137,2,92.00,54,'2026-07-02 22:47:16'),(547,62,137,2,91.00,51,'2026-07-02 22:47:16'),(548,61,137,0,34.00,48,'2026-07-02 22:47:16'),(549,60,137,1,68.00,45,'2026-07-02 22:47:16'),(550,59,137,1,67.00,42,'2026-07-02 22:47:16'),(551,58,137,2,87.00,39,'2026-07-02 22:47:16'),(552,57,137,2,86.00,36,'2026-07-02 22:47:16'),(553,56,137,0,29.00,33,'2026-07-02 22:47:16'),(554,55,137,1,63.00,30,'2026-07-02 22:47:16'),(555,54,137,1,62.00,27,'2026-07-02 22:47:16'),(556,1,137,0,24.00,18,'2026-07-02 22:47:16'),(557,65,138,0,39.00,50,'2026-07-02 22:47:16'),(558,64,138,1,73.00,46,'2026-07-02 22:47:16'),(559,63,138,1,72.00,42,'2026-07-02 22:47:16'),(560,62,138,2,92.00,38,'2026-07-02 22:47:16'),(561,61,138,2,91.00,34,'2026-07-02 22:47:16'),(562,60,138,0,34.00,30,'2026-07-02 22:47:16'),(563,59,138,1,68.00,26,'2026-07-02 22:47:16'),(564,58,138,1,67.00,22,'2026-07-02 22:47:16'),(565,57,138,2,87.00,18,'2026-07-02 22:47:16'),(566,56,138,2,86.00,89,'2026-07-02 22:47:16'),(567,55,138,0,29.00,85,'2026-07-02 22:47:16'),(568,54,138,1,63.00,81,'2026-07-02 22:47:16'),(569,1,138,2,87.00,19,'2026-07-02 22:47:16'),(570,65,139,2,82.00,40,'2026-07-02 22:47:16'),(571,64,139,0,39.00,35,'2026-07-02 22:47:16'),(572,63,139,1,73.00,30,'2026-07-02 22:47:16'),(573,62,139,1,72.00,25,'2026-07-02 22:47:16'),(574,61,139,2,92.00,20,'2026-07-02 22:47:16'),(575,60,139,2,91.00,15,'2026-07-02 22:47:16'),(576,59,139,0,34.00,85,'2026-07-02 22:47:16'),(577,58,139,1,68.00,80,'2026-07-02 22:47:16'),(578,57,139,1,67.00,75,'2026-07-02 22:47:16'),(579,56,139,2,87.00,70,'2026-07-02 22:47:16'),(580,55,139,2,86.00,65,'2026-07-02 22:47:16'),(581,54,139,0,29.00,60,'2026-07-02 22:47:16'),(582,1,139,2,88.00,20,'2026-07-02 22:47:16'),(583,65,140,2,83.00,30,'2026-07-02 22:47:16'),(584,64,140,2,82.00,24,'2026-07-02 22:47:16'),(585,63,140,0,39.00,18,'2026-07-02 22:47:16'),(586,62,140,1,73.00,87,'2026-07-02 22:47:16'),(587,61,140,1,72.00,81,'2026-07-02 22:47:16'),(588,60,140,2,92.00,75,'2026-07-02 22:47:16'),(589,59,140,2,91.00,69,'2026-07-02 22:47:16'),(590,58,140,0,34.00,63,'2026-07-02 22:47:16'),(591,57,140,1,68.00,57,'2026-07-02 22:47:16'),(592,56,140,1,67.00,51,'2026-07-02 22:47:16'),(593,55,140,2,87.00,45,'2026-07-02 22:47:16'),(594,54,140,2,86.00,39,'2026-07-02 22:47:16'),(595,1,140,1,62.00,21,'2026-07-02 22:47:16'),(596,65,141,1,77.00,20,'2026-07-02 22:47:16'),(597,64,141,2,83.00,88,'2026-07-02 22:47:16'),(598,63,141,2,82.00,81,'2026-07-02 22:47:16'),(599,62,141,0,39.00,74,'2026-07-02 22:47:16'),(600,61,141,1,73.00,67,'2026-07-02 22:47:16'),(601,60,141,1,72.00,60,'2026-07-02 22:47:16'),(602,59,141,2,92.00,53,'2026-07-02 22:47:16'),(603,58,141,2,91.00,46,'2026-07-02 22:47:16'),(604,57,141,0,34.00,39,'2026-07-02 22:47:16'),(605,56,141,1,68.00,32,'2026-07-02 22:47:16'),(606,55,141,1,67.00,25,'2026-07-02 22:47:16'),(607,54,141,2,87.00,18,'2026-07-02 22:47:16'),(608,1,141,1,63.00,22,'2026-07-02 22:47:16'),(609,65,142,1,78.00,85,'2026-07-02 22:47:16'),(610,64,142,1,77.00,77,'2026-07-02 22:47:16'),(611,63,142,2,83.00,69,'2026-07-02 22:47:16'),(612,62,142,2,82.00,61,'2026-07-02 22:47:16'),(613,61,142,0,39.00,53,'2026-07-02 22:47:16'),(614,60,142,1,73.00,45,'2026-07-02 22:47:16'),(615,59,142,1,72.00,37,'2026-07-02 22:47:16'),(616,58,142,2,92.00,29,'2026-07-02 22:47:16'),(617,57,142,2,91.00,21,'2026-07-02 22:47:16'),(618,56,142,0,34.00,88,'2026-07-02 22:47:16'),(619,55,142,1,68.00,80,'2026-07-02 22:47:16'),(620,54,142,1,67.00,72,'2026-07-02 22:47:16'),(621,1,142,0,29.00,23,'2026-07-02 22:47:16'),(622,65,143,0,44.00,75,'2026-07-02 22:47:16'),(623,64,143,1,78.00,66,'2026-07-02 22:47:16'),(624,63,143,1,77.00,57,'2026-07-02 22:47:16'),(625,62,143,2,83.00,48,'2026-07-02 22:47:16'),(626,61,143,2,82.00,39,'2026-07-02 22:47:16'),(627,60,143,0,39.00,30,'2026-07-02 22:47:16'),(628,59,143,1,73.00,21,'2026-07-02 22:47:16'),(629,58,143,1,72.00,87,'2026-07-02 22:47:16'),(630,57,143,2,92.00,78,'2026-07-02 22:47:16'),(631,56,143,2,91.00,69,'2026-07-02 22:47:16'),(632,55,143,0,34.00,60,'2026-07-02 22:47:16'),(633,54,143,1,68.00,51,'2026-07-02 22:47:16'),(634,1,143,2,92.00,24,'2026-07-02 22:47:16'),(635,65,144,2,87.00,65,'2026-07-02 22:47:16'),(636,64,144,0,44.00,55,'2026-07-02 22:47:16'),(637,63,144,1,78.00,45,'2026-07-02 22:47:16'),(638,62,144,1,77.00,35,'2026-07-02 22:47:16'),(639,61,144,2,83.00,25,'2026-07-02 22:47:16'),(640,60,144,2,82.00,15,'2026-07-02 22:47:16'),(641,59,144,0,39.00,80,'2026-07-02 22:47:16'),(642,58,144,1,73.00,70,'2026-07-02 22:47:16'),(643,57,144,1,72.00,60,'2026-07-02 22:47:16'),(644,56,144,2,92.00,50,'2026-07-02 22:47:16'),(645,55,144,2,91.00,40,'2026-07-02 22:47:16'),(646,54,144,0,34.00,30,'2026-07-02 22:47:16'),(647,1,144,2,93.00,25,'2026-07-02 22:47:16'),(648,65,145,2,88.00,55,'2026-07-02 22:47:16'),(649,64,145,2,87.00,44,'2026-07-02 22:47:16'),(650,63,145,0,44.00,33,'2026-07-02 22:47:16'),(651,62,145,1,78.00,22,'2026-07-02 22:47:16'),(652,61,145,1,77.00,86,'2026-07-02 22:47:16'),(653,60,145,2,83.00,75,'2026-07-02 22:47:16'),(654,59,145,2,82.00,64,'2026-07-02 22:47:16'),(655,58,145,0,39.00,53,'2026-07-02 22:47:16'),(656,57,145,1,73.00,42,'2026-07-02 22:47:16'),(657,56,145,1,72.00,31,'2026-07-02 22:47:16'),(658,55,145,2,92.00,20,'2026-07-02 22:47:16'),(659,54,145,2,91.00,84,'2026-07-02 22:47:16'),(660,1,145,1,67.00,26,'2026-07-02 22:47:16'),(661,65,146,1,57.00,45,'2026-07-02 22:47:16'),(662,64,146,2,88.00,33,'2026-07-02 22:47:16'),(663,63,146,2,87.00,21,'2026-07-02 22:47:16'),(664,62,146,0,44.00,84,'2026-07-02 22:47:16'),(665,61,146,1,78.00,72,'2026-07-02 22:47:16'),(666,60,146,1,77.00,60,'2026-07-02 22:47:16'),(667,59,146,2,83.00,48,'2026-07-02 22:47:16'),(668,58,146,2,82.00,36,'2026-07-02 22:47:16'),(669,57,146,0,39.00,24,'2026-07-02 22:47:16'),(670,56,146,1,73.00,87,'2026-07-02 22:47:16'),(671,55,146,1,72.00,75,'2026-07-02 22:47:16'),(672,54,146,2,92.00,63,'2026-07-02 22:47:16'),(673,1,146,1,68.00,27,'2026-07-02 22:47:16'),(674,65,147,1,58.00,35,'2026-07-02 22:47:16'),(675,64,147,1,57.00,22,'2026-07-02 22:47:16'),(676,63,147,2,88.00,84,'2026-07-02 22:47:16'),(677,62,147,2,87.00,71,'2026-07-02 22:47:16'),(678,61,147,0,44.00,58,'2026-07-02 22:47:16'),(679,60,147,1,78.00,45,'2026-07-02 22:47:16'),(680,59,147,1,77.00,32,'2026-07-02 22:47:16'),(681,58,147,2,83.00,19,'2026-07-02 22:47:16'),(682,57,147,2,82.00,81,'2026-07-02 22:47:16'),(683,56,147,0,39.00,68,'2026-07-02 22:47:16'),(684,55,147,1,73.00,55,'2026-07-02 22:47:16'),(685,54,147,1,72.00,42,'2026-07-02 22:47:16'),(686,1,147,0,34.00,28,'2026-07-02 22:47:16'),(687,65,148,0,24.00,25,'2026-07-02 22:47:16'),(688,64,148,1,58.00,86,'2026-07-02 22:47:16'),(689,63,148,1,57.00,72,'2026-07-02 22:47:16'),(690,62,148,2,88.00,58,'2026-07-02 22:47:16'),(691,61,148,2,87.00,44,'2026-07-02 22:47:16'),(692,60,148,0,44.00,30,'2026-07-02 22:47:16'),(693,59,148,1,78.00,16,'2026-07-02 22:47:16'),(694,58,148,1,77.00,77,'2026-07-02 22:47:16'),(695,57,148,2,83.00,63,'2026-07-02 22:47:16'),(696,56,148,2,82.00,49,'2026-07-02 22:47:16'),(697,55,148,0,39.00,35,'2026-07-02 22:47:16'),(698,54,148,1,73.00,21,'2026-07-02 22:47:16'),(699,1,148,2,83.00,29,'2026-07-02 22:47:16'),(700,65,149,2,92.00,15,'2026-07-02 22:47:16'),(701,64,149,0,24.00,75,'2026-07-02 22:47:16'),(702,63,149,1,58.00,60,'2026-07-02 22:47:16'),(703,62,149,1,57.00,45,'2026-07-02 22:47:16'),(704,61,149,2,88.00,30,'2026-07-02 22:47:16'),(705,60,149,2,87.00,15,'2026-07-02 22:47:16'),(706,59,149,0,44.00,75,'2026-07-02 22:47:16'),(707,58,149,1,78.00,60,'2026-07-02 22:47:16'),(708,57,149,1,77.00,45,'2026-07-02 22:47:16'),(709,56,149,2,83.00,30,'2026-07-02 22:47:16'),(710,55,149,2,82.00,15,'2026-07-02 22:47:16'),(711,54,149,0,39.00,75,'2026-07-02 22:47:16'),(712,1,149,2,84.00,30,'2026-07-02 22:47:16'),(713,65,150,2,93.00,80,'2026-07-02 22:47:16'),(714,64,150,2,92.00,64,'2026-07-02 22:47:16'),(715,63,150,0,24.00,48,'2026-07-02 22:47:16'),(716,62,150,1,58.00,32,'2026-07-02 22:47:16'),(717,61,150,1,57.00,16,'2026-07-02 22:47:16'),(718,60,150,2,88.00,75,'2026-07-02 22:47:16'),(719,59,150,2,87.00,59,'2026-07-02 22:47:16'),(720,58,150,0,44.00,43,'2026-07-02 22:47:16'),(721,57,150,1,78.00,27,'2026-07-02 22:47:16'),(722,56,150,1,77.00,86,'2026-07-02 22:47:16'),(723,55,150,2,83.00,70,'2026-07-02 22:47:16'),(724,54,150,2,82.00,54,'2026-07-02 22:47:16'),(725,1,150,1,72.00,31,'2026-07-02 22:47:16'),(726,65,151,1,62.00,70,'2026-07-02 22:47:16'),(727,64,151,2,93.00,53,'2026-07-02 22:47:16'),(728,63,151,2,92.00,36,'2026-07-02 22:47:16'),(729,62,151,0,24.00,19,'2026-07-02 22:47:16'),(730,61,151,1,58.00,77,'2026-07-02 22:47:16'),(731,60,151,1,57.00,60,'2026-07-02 22:47:16'),(732,59,151,2,88.00,43,'2026-07-02 22:47:16'),(733,58,151,2,87.00,26,'2026-07-02 22:47:16'),(734,57,151,0,44.00,84,'2026-07-02 22:47:16'),(735,56,151,1,78.00,67,'2026-07-02 22:47:16'),(736,55,151,1,77.00,50,'2026-07-02 22:47:16'),(737,54,151,2,83.00,33,'2026-07-02 22:47:16'),(738,1,151,1,73.00,32,'2026-07-02 22:47:16'),(739,65,152,1,63.00,60,'2026-07-02 22:47:16'),(740,64,152,1,62.00,42,'2026-07-02 22:47:16'),(741,63,152,2,93.00,24,'2026-07-02 22:47:16'),(742,62,152,2,92.00,81,'2026-07-02 22:47:16'),(743,61,152,0,24.00,63,'2026-07-02 22:47:16'),(744,60,152,1,58.00,45,'2026-07-02 22:47:16'),(745,59,152,1,57.00,27,'2026-07-02 22:47:16'),(746,58,152,2,88.00,84,'2026-07-02 22:47:16'),(747,57,152,2,87.00,66,'2026-07-02 22:47:16'),(748,56,152,0,44.00,48,'2026-07-02 22:47:16'),(749,55,152,1,78.00,30,'2026-07-02 22:47:16'),(750,54,152,1,77.00,87,'2026-07-02 22:47:16'),(751,1,152,0,39.00,33,'2026-07-02 22:47:16'),(752,65,153,0,29.00,50,'2026-07-02 22:47:16'),(753,64,153,1,63.00,31,'2026-07-02 22:47:16'),(754,63,153,1,62.00,87,'2026-07-02 22:47:16'),(755,62,153,2,93.00,68,'2026-07-02 22:47:16'),(756,61,153,2,92.00,49,'2026-07-02 22:47:16'),(757,60,153,0,24.00,30,'2026-07-02 22:47:16'),(758,59,153,1,58.00,86,'2026-07-02 22:47:16'),(759,58,153,1,57.00,67,'2026-07-02 22:47:16'),(760,57,153,2,88.00,48,'2026-07-02 22:47:16'),(761,56,153,2,87.00,29,'2026-07-02 22:47:16'),(762,55,153,0,44.00,85,'2026-07-02 22:47:16'),(763,54,153,1,78.00,66,'2026-07-02 22:47:16'),(764,1,153,2,88.00,34,'2026-07-02 22:47:16'),(765,65,154,2,83.00,40,'2026-07-02 22:47:16'),(766,64,154,0,29.00,20,'2026-07-02 22:47:16'),(767,63,154,1,63.00,75,'2026-07-02 22:47:16'),(768,62,154,1,62.00,55,'2026-07-02 22:47:16'),(769,61,154,2,93.00,35,'2026-07-02 22:47:16'),(770,60,154,2,92.00,15,'2026-07-02 22:47:16'),(771,59,154,0,24.00,70,'2026-07-02 22:47:16'),(772,58,154,1,58.00,50,'2026-07-02 22:47:16'),(773,57,154,1,57.00,30,'2026-07-02 22:47:16'),(774,56,154,2,88.00,85,'2026-07-02 22:47:16'),(775,55,154,2,87.00,65,'2026-07-02 22:47:16'),(776,54,154,0,44.00,45,'2026-07-02 22:47:16'),(777,1,154,2,89.00,35,'2026-07-02 22:47:16'),(778,65,155,2,84.00,30,'2026-07-02 22:47:16'),(779,64,155,2,83.00,84,'2026-07-02 22:47:16'),(780,63,155,0,29.00,63,'2026-07-02 22:47:16'),(781,62,155,1,63.00,42,'2026-07-02 22:47:16'),(782,61,155,1,62.00,21,'2026-07-02 22:47:16'),(783,60,155,2,93.00,75,'2026-07-02 22:47:16'),(784,59,155,2,92.00,54,'2026-07-02 22:47:16'),(785,58,155,0,24.00,33,'2026-07-02 22:47:16'),(786,57,155,1,58.00,87,'2026-07-02 22:47:16'),(787,56,155,1,57.00,66,'2026-07-02 22:47:16'),(788,55,155,2,88.00,45,'2026-07-02 22:47:16'),(789,54,155,2,87.00,24,'2026-07-02 22:47:16'),(790,1,155,1,77.00,36,'2026-07-02 22:47:16'),(791,65,156,1,67.00,20,'2026-07-02 22:47:16'),(792,64,156,2,84.00,73,'2026-07-02 22:47:16'),(793,63,156,2,83.00,51,'2026-07-02 22:47:16'),(794,62,156,0,29.00,29,'2026-07-02 22:47:16'),(795,61,156,1,63.00,82,'2026-07-02 22:47:16'),(796,60,156,1,62.00,60,'2026-07-02 22:47:16'),(797,59,156,2,93.00,38,'2026-07-02 22:47:16'),(798,58,156,2,92.00,16,'2026-07-02 22:47:16'),(799,57,156,0,24.00,69,'2026-07-02 22:47:16'),(800,56,156,1,58.00,47,'2026-07-02 22:47:16'),(801,55,156,1,57.00,25,'2026-07-02 22:47:16'),(802,54,156,2,88.00,78,'2026-07-02 22:47:16'),(803,1,156,1,78.00,37,'2026-07-02 22:47:16'),(804,65,157,1,68.00,85,'2026-07-02 22:47:16'),(805,64,157,1,67.00,62,'2026-07-02 22:47:16'),(806,63,157,2,84.00,39,'2026-07-02 22:47:16'),(807,62,157,2,83.00,16,'2026-07-02 22:47:16'),(808,61,157,0,29.00,68,'2026-07-02 22:47:16'),(809,60,157,1,63.00,45,'2026-07-02 22:47:16'),(810,59,157,1,62.00,22,'2026-07-02 22:47:16'),(811,58,157,2,93.00,74,'2026-07-02 22:47:16'),(812,57,157,2,92.00,51,'2026-07-02 22:47:16'),(813,56,157,0,24.00,28,'2026-07-02 22:47:16'),(814,55,157,1,58.00,80,'2026-07-02 22:47:16'),(815,54,157,1,57.00,57,'2026-07-02 22:47:16'),(816,1,157,0,44.00,38,'2026-07-02 22:47:16'),(817,65,158,0,34.00,75,'2026-07-02 22:47:16'),(818,64,158,1,68.00,51,'2026-07-02 22:47:16'),(819,63,158,1,67.00,27,'2026-07-02 22:47:16'),(820,62,158,2,84.00,78,'2026-07-02 22:47:16'),(821,61,158,2,83.00,54,'2026-07-02 22:47:16'),(822,60,158,0,29.00,30,'2026-07-02 22:47:16'),(823,59,158,1,63.00,81,'2026-07-02 22:47:16'),(824,58,158,1,62.00,57,'2026-07-02 22:47:16'),(825,57,158,2,93.00,33,'2026-07-02 22:47:16'),(826,56,158,2,92.00,84,'2026-07-02 22:47:16'),(827,55,158,0,24.00,60,'2026-07-02 22:47:16'),(828,54,158,1,58.00,36,'2026-07-02 22:47:16'),(829,1,158,2,93.00,39,'2026-07-02 22:47:16'),(830,65,159,2,88.00,65,'2026-07-02 22:47:16'),(831,64,159,0,34.00,40,'2026-07-02 22:47:16'),(832,63,159,1,68.00,15,'2026-07-02 22:47:16'),(833,62,159,1,67.00,65,'2026-07-02 22:47:16'),(834,61,159,2,84.00,40,'2026-07-02 22:47:16'),(835,60,159,2,83.00,15,'2026-07-02 22:47:16'),(836,59,159,0,29.00,65,'2026-07-02 22:47:16'),(837,58,159,1,63.00,40,'2026-07-02 22:47:16'),(838,57,159,1,62.00,15,'2026-07-02 22:47:16'),(839,56,159,2,93.00,65,'2026-07-02 22:47:16'),(840,55,159,2,92.00,40,'2026-07-02 22:47:16'),(841,54,159,0,24.00,15,'2026-07-02 22:47:16'),(842,1,159,2,94.00,40,'2026-07-02 22:47:16'),(843,65,160,2,89.00,55,'2026-07-02 22:47:16'),(844,64,160,2,88.00,29,'2026-07-02 22:47:16'),(845,63,160,0,34.00,78,'2026-07-02 22:47:16'),(846,62,160,1,68.00,52,'2026-07-02 22:47:16'),(847,61,160,1,67.00,26,'2026-07-02 22:47:16'),(848,60,160,2,84.00,75,'2026-07-02 22:47:16'),(849,59,160,2,83.00,49,'2026-07-02 22:47:16'),(850,58,160,0,29.00,23,'2026-07-02 22:47:16'),(851,57,160,1,63.00,72,'2026-07-02 22:47:16'),(852,56,160,1,62.00,46,'2026-07-02 22:47:16'),(853,55,160,2,93.00,20,'2026-07-02 22:47:16'),(854,54,160,2,92.00,69,'2026-07-02 22:47:16'),(855,1,160,1,57.00,41,'2026-07-02 22:47:16'),(856,65,161,1,72.00,45,'2026-07-02 22:47:16'),(857,64,161,2,89.00,18,'2026-07-02 22:47:16'),(858,63,161,2,88.00,66,'2026-07-02 22:47:16'),(859,62,161,0,34.00,39,'2026-07-02 22:47:16'),(860,61,161,1,68.00,87,'2026-07-02 22:47:16'),(861,60,161,1,67.00,60,'2026-07-02 22:47:16'),(862,59,161,2,84.00,33,'2026-07-02 22:47:16'),(863,58,161,2,83.00,81,'2026-07-02 22:47:16'),(864,57,161,0,29.00,54,'2026-07-02 22:47:16'),(865,56,161,1,63.00,27,'2026-07-02 22:47:16'),(866,55,161,1,62.00,75,'2026-07-02 22:47:16'),(867,54,161,2,93.00,48,'2026-07-02 22:47:16'),(868,1,161,1,58.00,42,'2026-07-02 22:47:16'),(869,65,162,1,73.00,35,'2026-07-02 22:47:16'),(870,64,162,1,72.00,82,'2026-07-02 22:47:16'),(871,63,162,2,89.00,54,'2026-07-02 22:47:16'),(872,62,162,2,88.00,26,'2026-07-02 22:47:16'),(873,61,162,0,34.00,73,'2026-07-02 22:47:16'),(874,60,162,1,68.00,45,'2026-07-02 22:47:16'),(875,59,162,1,67.00,17,'2026-07-02 22:47:16'),(876,58,162,2,84.00,64,'2026-07-02 22:47:16'),(877,57,162,2,83.00,36,'2026-07-02 22:47:16'),(878,56,162,0,29.00,83,'2026-07-02 22:47:16'),(879,55,162,1,63.00,55,'2026-07-02 22:47:16'),(880,54,162,1,62.00,27,'2026-07-02 22:47:16'),(881,1,162,0,24.00,43,'2026-07-02 22:47:16');
/*!40000 ALTER TABLE `study_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `study_path`
--

LOCK TABLES `study_path` WRITE;
/*!40000 ALTER TABLE `study_path` DISABLE KEYS */;
INSERT  IGNORE INTO `study_path` (`id`, `user_id`, `target_node_id`, `path_name`, `total_nodes`, `total_minutes`, `ai_summary`, `status`, `create_time`) VALUES (1,1,3,'一次函数提升路径',3,120,'建议先巩固有理数运算，再复习一元一次方程，最后学习一次函数图像与表达式。',0,'2026-07-02 22:02:07');
/*!40000 ALTER TABLE `study_path` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `path_detail`
--

LOCK TABLES `path_detail` WRITE;
/*!40000 ALTER TABLE `path_detail` DISABLE KEYS */;
INSERT  IGNORE INTO `path_detail` (`id`, `path_id`, `node_id`, `sort_order`, `is_finished`) VALUES (1,1,1,1,1),(2,1,2,2,0),(3,1,3,3,0);
/*!40000 ALTER TABLE `path_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `exam`
--

LOCK TABLES `exam` WRITE;
/*!40000 ALTER TABLE `exam` DISABLE KEYS */;
INSERT IGNORE INTO `exam` (`id`, `exam_name`, `course_id`, `creator_id`, `total_score`, `status`, `create_time`) VALUES
(1,'初中代数基础阶段测评',1,2,100,1,'2026-07-02 22:10:00'),
(2,'运动和相互作用诊断测评',20,52,100,1,'2026-07-02 22:50:00'),
(3,'数据与算法实践阶段测评',22,53,100,1,'2026-07-02 22:55:00');
/*!40000 ALTER TABLE `exam` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `exam_question`
--

LOCK TABLES `exam_question` WRITE;
/*!40000 ALTER TABLE `exam_question` DISABLE KEYS */;
INSERT IGNORE INTO `exam_question` (`id`, `exam_id`, `question_id`, `sort_order`) VALUES
(1,1,1,1),(2,1,2,2),(3,1,3,3),
(4,2,88,1),(5,2,89,2),(6,2,90,3),(7,2,91,4),(8,2,92,5),
(9,3,104,1),(10,3,105,2),(11,3,106,3),(12,3,107,4),(13,3,108,5);
/*!40000 ALTER TABLE `exam_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `exam_record`
--

LOCK TABLES `exam_record` WRITE;
/*!40000 ALTER TABLE `exam_record` DISABLE KEYS */;
INSERT  IGNORE INTO `exam_record` (`id`, `user_id`, `course_id`, `total_score`, `user_score`, `ai_report`, `create_time`) VALUES (1,1,1,100,78.00,'基础运算掌握较好，方程应用题还需要加强建模训练。','2026-07-02 22:02:07'),(65,1,22,100,81.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(66,1,20,100,67.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(67,1,18,100,96.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(68,54,22,100,91.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(69,54,20,100,77.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(70,54,18,100,63.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(71,55,22,100,92.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(72,55,20,100,78.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(73,55,18,100,64.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(74,56,22,100,93.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(75,56,20,100,79.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(76,56,18,100,65.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(77,57,22,100,94.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(78,57,20,100,80.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(79,57,18,100,66.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(80,58,22,100,95.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(81,58,20,100,81.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(82,58,18,100,67.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(83,59,22,100,96.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(84,59,20,100,82.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(85,59,18,100,68.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(86,60,22,100,97.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(87,60,20,100,83.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(88,60,18,100,69.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(89,61,22,100,55.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(90,61,20,100,84.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(91,61,18,100,70.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(92,62,22,100,56.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(93,62,20,100,85.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(94,62,18,100,71.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(95,63,22,100,57.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(96,63,20,100,86.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(97,63,18,100,72.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(98,64,22,100,58.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(99,64,20,100,87.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(100,64,18,100,73.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(101,65,22,100,59.00,'系统诊断：数据与算法实践 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(102,65,20,100,88.00,'系统诊断：运动和相互作用 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16'),(103,65,18,100,74.00,'系统诊断：数与代数综合 已完成阶段测评，建议结合薄弱知识点继续练习。','2026-07-02 22:47:16');
/*!40000 ALTER TABLE `exam_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `wrong_question`
--

LOCK TABLES `wrong_question` WRITE;
/*!40000 ALTER TABLE `wrong_question` DISABLE KEYS */;
INSERT  IGNORE INTO `wrong_question` (`id`, `user_id`, `question_id`, `wrong_answer`, `wrong_count`, `ai_explain`, `create_time`) VALUES (1,1,2,'B',1,'移项后要继续除以未知数系数，注意不要把 2x=8 误判为 x=3。','2026-07-02 22:02:07'),(2,57,119,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(3,58,118,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(4,59,117,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(5,60,116,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(6,61,115,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(7,62,114,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(8,63,113,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(9,64,112,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(10,65,111,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(11,54,111,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(12,55,110,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(13,56,109,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(14,1,109,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(15,57,108,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(16,58,107,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(17,59,106,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(18,60,105,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(19,61,104,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(20,62,103,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(21,63,102,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(22,64,101,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(23,65,100,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(24,54,100,'A',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(25,55,99,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(26,56,98,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(27,1,98,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(28,57,97,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(29,58,96,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(30,59,95,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(31,60,94,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(32,61,93,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(33,62,92,'A',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(34,63,91,'A',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(35,64,90,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(36,65,89,'B',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(37,54,89,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(38,55,88,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(39,56,87,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(40,1,87,'A',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(41,57,86,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(42,58,85,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(43,59,84,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(44,60,83,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(45,61,82,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(46,62,81,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(47,63,80,'B',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(48,64,79,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(49,65,78,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(50,54,78,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(51,55,77,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(52,56,76,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(53,1,76,'A',3,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(54,57,75,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(55,58,74,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(56,59,73,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(57,60,72,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(58,58,8,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(59,59,7,'B',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(60,60,6,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(61,61,5,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(62,62,4,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(63,63,3,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(64,64,2,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(65,65,1,'A',1,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16'),(66,54,1,'A',2,'建议回到关联知识点重新学习概念，并完成同类型题目巩固。','2026-07-02 22:47:16');
/*!40000 ALTER TABLE `wrong_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `cross_subject_theme`
--

LOCK TABLES `cross_subject_theme` WRITE;
/*!40000 ALTER TABLE `cross_subject_theme` DISABLE KEYS */;
INSERT  IGNORE INTO `cross_subject_theme` (`id`, `theme_name`, `description`, `difficulty`, `publisher_id`, `status`, `create_time`) VALUES (1,'校园运动会数据分析','结合数学函数、物理速度和 Python 数据处理，分析运动会成绩变化。',2,2,1,'2026-07-02 22:02:07'),(2,'校园节能用电分析','结合电功率、统计图表和数据处理，分析教室节能方案。',3,53,1,'2026-07-02 22:47:16'),(3,'体育测试成绩建模','结合速度、函数模型和程序化统计，分析体育测试表现。',2,51,1,'2026-07-02 22:47:16');
/*!40000 ALTER TABLE `cross_subject_theme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `cross_theme_node`
--

LOCK TABLES `cross_theme_node` WRITE;
/*!40000 ALTER TABLE `cross_theme_node` DISABLE KEYS */;
INSERT  IGNORE INTO `cross_theme_node` (`id`, `theme_id`, `node_id`) VALUES (1,1,3),(2,1,4),(3,1,8),(4,2,149),(5,2,166),(6,2,168),(7,2,181),(11,3,140),(12,3,151),(13,3,152),(14,3,172),(15,3,180);
/*!40000 ALTER TABLE `cross_theme_node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT  IGNORE INTO `sys_notice` (`id`, `title`, `content`, `publisher_id`, `target_role`, `create_time`) VALUES (1,'系统测试数据已导入','当前环境已准备好学生、教师、课程、知识图谱、题库和学习记录测试数据。',5,'all','2026-07-02 22:02:07'),(2,'公开课程主题测试数据已扩展','已根据公开课程标准主题扩展课程、知识图谱、题库、学习记录和测评记录，便于演示。',5,'all','2026-07-02 22:47:16');
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sys_oper_log`
--

LOCK TABLES `sys_oper_log` WRITE;
/*!40000 ALTER TABLE `sys_oper_log` DISABLE KEYS */;
INSERT  IGNORE INTO `sys_oper_log` (`id`, `user_id`, `module`, `operation`, `ip`, `create_time`) VALUES (1,5,'数据库','导入测试数据','127.0.0.1','2026-07-02 22:02:07'),(2,5,'数据库','导入公开课程主题扩展数据','127.0.0.1','2026-07-02 22:47:16');
/*!40000 ALTER TABLE `sys_oper_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `ai_call_log`
--

LOCK TABLES `ai_call_log` WRITE;
/*!40000 ALTER TABLE `ai_call_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_call_log` ENABLE KEYS */;
UNLOCK TABLES;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 第 3 部分：学习资料 enrichment（来自 enrich_learning_materials.sql）
-- ============================================================

USE kg_tutoring_db;

START TRANSACTION;

UPDATE knowledge_node
SET description = CONCAT(
    '## 学习目标\n',
    '- 说清楚「', name, '」要解决的核心问题。\n',
    '- 能把本知识点和章节「', COALESCE(chapter, '未分章'), '」中的前后内容联系起来。\n',
    '- 能完成基础题，并能解释每一步使用了哪个概念或规则。\n\n',
    '## 核心概念\n',
    COALESCE(NULLIF(description, ''), CONCAT('围绕「', name, '」建立基础理解，先掌握定义，再练习典型应用。')), '\n\n',
    '## 方法提示\n',
    CASE
        WHEN course_id IN (1, 18) OR name REGEXP '有理数|整式|方程|不等式|函数|坐标|统计|趋势' THEN CONCAT(
            '- 先把题目中的数量关系翻译成数学语言，分清已知量、未知量和目标量。\n',
            '- 遇到计算题，按“列式、化简、求解、检验”四步写清楚过程。\n',
            '- 遇到函数或统计问题，优先画表格、数轴、坐标图或统计图，把变化关系看出来。\n',
            '- 复习时关注概念之间的承接：数的运算支撑代数式，方程支撑函数和模型。'
        )
        WHEN course_id = 19 OR name REGEXP '线段|角|三角形|全等|轴对称|平移|四边形|圆|概率|统计图表' THEN CONCAT(
            '- 先画图，再标条件，把已知边、角、位置关系和目标结论写在图旁。\n',
            '- 几何证明要说明“为什么”，常用路径是找全等、找平行、找角度关系或找特殊图形性质。\n',
            '- 统计和概率题先判断数据类型：比较、趋势、比例、集中趋势或随机事件。\n',
            '- 每做完一题，检查图形变换是否保持形状、大小、对应点和对应边关系。'
        )
        WHEN course_id IN (2, 20, 21) OR name REGEXP '运动|速度|密度|力|重力|摩擦|平衡|压强|浮力|功|功率|能|热|电路|电流|电压|电阻|欧姆|电功率' THEN CONCAT(
            '- 先明确研究对象和物理情境，再列出已知量、单位和要求的物理量。\n',
            '- 常用关系包括：速度 `v=s/t`，密度 `ρ=m/V`，压强 `p=F/S`，功率 `P=W/t`，欧姆定律 `I=U/R`。\n',
            '- 力学题要画受力示意图，电学题要先判断串并联关系，图像题要读清横轴和纵轴含义。\n',
            '- 计算完成后检查单位是否统一，结论是否符合生活经验和物理意义。'
        )
        WHEN course_id IN (3, 22) OR name REGEXP '数据|流程图|变量|表达式|条件|循环|列表|排序|算法|程序' THEN CONCAT(
            '- 先把问题拆成输入、处理、输出三部分，再决定使用变量、分支、循环还是列表。\n',
            '- 写 Python 时注意赋值用 `=`，比较用 `==`，条件语句和循环语句后面要写冒号。\n',
            '- 循环题要明确循环变量、循环范围、更新规则和停止条件，避免多算一次或少算一次。\n',
            '- 数据处理题先观察数据格式，再做清洗、筛选、统计和可视化。'
        )
        ELSE CONCAT(
            '- 先从真实情境提出问题，再判断需要哪些数学、物理或信息科技工具。\n',
            '- 建模时说明假设条件，采集数据时记录来源、单位和可能误差。\n',
            '- 展示结论时同时给出图表、计算过程和解释文字，让证据链完整。\n',
            '- 评价方案时关注数据是否充分、模型是否合理、结论是否可复查。'
        )
    END,
    '\n\n',
    '## 学习步骤\n',
    '1. 先读定义：用自己的话复述「', name, '」的含义，标出关键词。\n',
    '2. 再看关系：写出它和本章其他知识点的联系，例如前置概念、常见公式、图像或操作规则。\n',
    '3. 做一个例子：从最简单的数据、图形或题干开始，完整写出推理过程。\n',
    '4. 变式练习：改变一个条件，观察结论、计算步骤或程序输出是否变化。\n',
    '5. 复盘总结：记录一道错题或易混点，说明下次如何避免。\n\n',
    '## 例题导学\n',
    '请围绕「', name, '」自拟一个简单情境：先列出已知条件，再写出要求什么，最后用本知识点完成求解或解释。完成后检查答案是否符合题意、单位或逻辑关系。\n\n',
    '## 常见误区\n',
    '- 只记结论，不说明条件和适用范围。\n',
    '- 做题时跳步，导致符号、单位、方向或变量含义混乱。\n',
    '- 看到新题型时不能迁移，说明还没有真正理解概念之间的关系。\n\n',
    '## 练习建议\n',
    CASE
        WHEN difficulty IS NULL OR difficulty <= 1 THEN '先完成 3 道概念判断题和 2 道基础应用题，重点检查定义是否理解准确。'
        WHEN difficulty = 2 THEN '先完成 2 道基础题，再做 3 道变式题，重点训练条件变化后的处理方法。'
        ELSE '先复习前置知识，再完成 2 道综合题和 1 道开放解释题，重点写清楚推理链条。'
    END,
    '\n\n',
    '## 自测清单\n',
    '- 我能不看资料解释「', name, '」是什么。\n',
    '- 我能指出一道题中哪些信息和本知识点有关。\n',
    '- 我能独立完成一道基础题，并说明每一步理由。\n',
    '- 我能说出一个容易犯错的地方。'
)
WHERE name IS NOT NULL
  AND (description IS NULL OR description NOT LIKE '## 学习目标%');

COMMIT;

-- 补充学生端知识点练习题：每个知识点增加 1 道多选题和 1 道判断题
INSERT  IGNORE INTO `question` (`id`, `node_id`, `content`, `options`, `answer`, `analysis`, `difficulty`, `question_type`, `create_time`) VALUES
(120,1,'学习“有理数运算”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','有理数运算 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(121,1,'判断：学习“有理数运算”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(122,2,'学习“一元一次方程”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','一元一次方程 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(123,2,'判断：学习“一元一次方程”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(124,3,'学习“一次函数”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','一次函数 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(125,3,'判断：学习“一次函数”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(126,4,'分析“速度与路程”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','速度与路程 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(127,4,'判断：解决“速度与路程”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(128,5,'分析“力的合成”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','力的合成 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(129,5,'判断：解决“力的合成”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(130,6,'完成“变量与表达式”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','变量与表达式 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(131,6,'判断：在“变量与表达式”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(132,7,'完成“条件分支”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','条件分支 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(133,7,'判断：在“条件分支”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(134,8,'完成“循环结构”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','循环结构 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(135,8,'判断：在“循环结构”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(136,135,'学习“整数与有理数”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','整数与有理数 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(137,135,'判断：学习“整数与有理数”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(138,136,'学习“整式加减”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','整式加减 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(139,136,'判断：学习“整式加减”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(140,137,'学习“一元一次方程应用”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','一元一次方程应用 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(141,137,'判断：学习“一元一次方程应用”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(142,138,'学习“不等式与不等式组”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','不等式与不等式组 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(143,138,'判断：学习“不等式与不等式组”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(144,139,'学习“平面直角坐标系”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','平面直角坐标系 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(145,139,'判断：学习“平面直角坐标系”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(146,140,'学习“一次函数模型”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','一次函数模型 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(147,140,'判断：学习“一次函数模型”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(148,141,'学习“二元一次方程组”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','二元一次方程组 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(149,141,'判断：学习“二元一次方程组”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(150,142,'学习“数据的集中趋势”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','数据的集中趋势 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(151,142,'判断：学习“数据的集中趋势”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(152,143,'学习“线段角与相交线”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','线段角与相交线 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(153,143,'判断：学习“线段角与相交线”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(154,144,'学习“三角形性质”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','三角形性质 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(155,144,'判断：学习“三角形性质”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(156,145,'学习“全等三角形”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','全等三角形 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(157,145,'判断：学习“全等三角形”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(158,146,'学习“轴对称与平移”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','轴对称与平移 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(159,146,'判断：学习“轴对称与平移”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(160,147,'学习“四边形基础”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','四边形基础 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(161,147,'判断：学习“四边形基础”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(162,148,'学习“圆的初步认识”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','圆的初步认识 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(163,148,'判断：学习“圆的初步认识”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(164,149,'学习“统计图表读取”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','统计图表读取 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(165,149,'判断：学习“统计图表读取”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(166,150,'学习“概率初步”时，哪些做法有助于正确解题？','{\"A\":\"先明确已知量和未知量\",\"B\":\"把数量关系写成式子、方程、图形或统计量\",\"C\":\"计算后结合题意检验结果\",\"D\":\"只看选项长度直接猜答案\"}','A,B,C','概率初步 的练习应先读题建模，再计算或推理，最后回到题意检查。',2,'multi','2026-07-11 10:00:00'),
(167,150,'判断：学习“概率初步”时，只记住一个结论，不理解适用条件，也能稳定解决所有题目。','{\"A\":\"正确\",\"B\":\"错误\"}','B','结论必须和适用条件、解题步骤一起理解，否则容易误用。',1,'judge','2026-07-11 10:00:00'),
(168,151,'分析“机械运动描述”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','机械运动描述 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(169,151,'判断：解决“机械运动描述”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(170,152,'分析“速度图像”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','速度图像 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(171,152,'判断：解决“速度图像”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(172,153,'分析“质量与密度”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','质量与密度 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(173,153,'判断：解决“质量与密度”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(174,154,'分析“力和弹力”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','力和弹力 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(175,154,'判断：解决“力和弹力”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(176,155,'分析“重力与摩擦力”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','重力与摩擦力 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(177,155,'判断：解决“重力与摩擦力”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(178,156,'分析“二力平衡”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','二力平衡 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(179,156,'判断：解决“二力平衡”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(180,157,'分析“压强”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','压强 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(181,157,'判断：解决“压强”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(182,158,'分析“浮力”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','浮力 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(183,158,'判断：解决“浮力”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(184,159,'分析“功和功率”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','功和功率 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(185,159,'判断：解决“功和功率”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(186,160,'分析“动能和势能”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','动能和势能 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(187,160,'判断：解决“动能和势能”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(188,161,'分析“内能与热量”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','内能与热量 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(189,161,'判断：解决“内能与热量”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(190,162,'分析“简单电路”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','简单电路 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(191,162,'判断：解决“简单电路”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(192,163,'分析“电流和电压”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','电流和电压 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(193,163,'判断：解决“电流和电压”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(194,164,'分析“电阻”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','电阻 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(195,164,'判断：解决“电阻”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(196,165,'分析“欧姆定律”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','欧姆定律 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(197,165,'判断：解决“欧姆定律”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(198,166,'分析“电功率”相关物理问题时，哪些步骤是合理的？','{\"A\":\"明确研究对象\",\"B\":\"标出相关物理量和单位\",\"C\":\"根据条件选择合适规律或公式\",\"D\":\"忽略单位直接套数值\"}','A,B,C','电功率 题目要先确定对象、物理量和单位，再选择规律分析。',2,'multi','2026-07-11 10:00:00'),
(199,166,'判断：解决“电功率”问题时，单位换算和方向判断可以完全忽略。','{\"A\":\"正确\",\"B\":\"错误\"}','B','物理问题中单位、方向和条件都会影响结果，不能忽略。',1,'judge','2026-07-11 10:00:00'),
(200,167,'完成“数据采集”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','数据采集 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(201,167,'判断：在“数据采集”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(202,168,'完成“数据整理”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','数据整理 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(203,168,'判断：在“数据整理”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(204,169,'完成“流程图”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','流程图 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(205,169,'判断：在“流程图”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(206,170,'完成“变量和表达式”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','变量和表达式 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(207,170,'判断：在“变量和表达式”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(208,171,'完成“条件结构”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','条件结构 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(209,171,'判断：在“条件结构”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(210,172,'完成“循环结构”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','循环结构 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(211,172,'判断：在“循环结构”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(212,173,'完成“列表数据处理”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','列表数据处理 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(213,173,'判断：在“列表数据处理”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(214,174,'完成“简单排序算法”相关任务时，哪些做法更可靠？','{\"A\":\"先理解输入和输出\",\"B\":\"把步骤拆成清晰的算法流程\",\"C\":\"用样例检查程序或方案\",\"D\":\"不测试就默认结果正确\"}','A,B,C','简单排序算法 任务需要明确输入输出、设计步骤，并用样例验证。',2,'multi','2026-07-11 10:00:00'),
(215,174,'判断：在“简单排序算法”学习中，只要程序能运行一次，就一定说明逻辑完全正确。','{\"A\":\"正确\",\"B\":\"错误\"}','B','程序能运行不等于逻辑覆盖所有情况，还需要用样例和边界情况验证。',1,'judge','2026-07-11 10:00:00'),
(216,175,'开展“提出研究问题”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','提出研究问题 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(217,175,'判断：在“提出研究问题”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00'),
(218,176,'开展“设计调查表”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','设计调查表 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(219,176,'判断：在“设计调查表”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00'),
(220,177,'开展“数据可视化”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','数据可视化 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(221,177,'判断：在“数据可视化”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00'),
(222,178,'开展“建立数学模型”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','建立数学模型 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(223,178,'判断：在“建立数学模型”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00'),
(224,179,'开展“物理量测量”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','物理量测量 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(225,179,'判断：在“物理量测量”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00'),
(226,180,'开展“程序化统计”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','程序化统计 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(227,180,'判断：在“程序化统计”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00'),
(228,181,'开展“解释与评价”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','解释与评价 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(229,181,'判断：在“解释与评价”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00'),
(230,182,'开展“项目展示”相关项目时，哪些做法是合理的？','{\"A\":\"明确研究问题\",\"B\":\"收集和整理证据\",\"C\":\"结合数学、物理或信息科技方法解释结果\",\"D\":\"不说明依据直接给结论\"}','A,B,C','项目展示 项目应围绕问题收集证据，并用合适学科方法解释。',2,'multi','2026-07-11 10:00:00'),
(231,182,'判断：在“项目展示”项目中，结论不需要数据或过程支持。','{\"A\":\"正确\",\"B\":\"错误\"}','B','项目结论需要证据、方法和过程支持，不能只给主观判断。',1,'judge','2026-07-11 10:00:00');

-- 一键初始化补充：迁移章节表结构，匹配当前后端实体
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

INSERT IGNORE INTO chapter (course_id, chapter_name, sort)
SELECT DISTINCT course_id, chapter, 1
FROM knowledge_node
WHERE chapter IS NOT NULL AND chapter != '';

UPDATE knowledge_node n
INNER JOIN chapter c ON n.course_id = c.course_id AND n.chapter = c.chapter_name
SET n.chapter_id = c.id
WHERE n.chapter_id IS NULL AND n.chapter IS NOT NULL;

ALTER TABLE knowledge_node MODIFY COLUMN chapter_id INT NOT NULL;

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

USE kg_tutoring_db;

-- 将每个知识点的练习题补齐到 7 道左右。
-- 按当前题量动态补充，重复执行时不会继续增加。
INSERT INTO question (node_id, content, options, answer, analysis, difficulty, question_type, create_time)
SELECT
    nq.node_id,
    CASE t.seq
        WHEN 1 THEN CONCAT('围绕“', nq.node_name, '”，学习前最应该先确认什么？')
        WHEN 2 THEN CONCAT('学习“', nq.node_name, '”时，哪些做法能帮助形成稳定理解？')
        WHEN 3 THEN CONCAT('判断：只要背下“', nq.node_name, '”的结论，不理解条件也能稳定迁移应用。')
        WHEN 4 THEN CONCAT('关于“', nq.node_name, '”的学习过程，下列哪项最能体现有效复盘？')
        WHEN 5 THEN CONCAT('在解决“', nq.node_name, '”相关问题时，哪些检查步骤是合理的？')
        WHEN 6 THEN CONCAT('判断：完成“', nq.node_name, '”练习后，应结合错因和解析修正自己的理解。')
        ELSE CONCAT('遇到“', nq.node_name, '”综合应用题时，比较稳妥的处理顺序是什么？')
    END AS content,
    CASE t.seq
        WHEN 1 THEN '{\"A\":\"知识点名称、目标和已知基础\",\"B\":\"只看答案选项\",\"C\":\"先跳过题干\",\"D\":\"随机选择一个公式\"}'
        WHEN 2 THEN '{\"A\":\"梳理核心概念\",\"B\":\"结合例题说明适用条件\",\"C\":\"用练习检验理解\",\"D\":\"只背答案字母\"}'
        WHEN 3 THEN '{\"A\":\"正确\",\"B\":\"错误\"}'
        WHEN 4 THEN '{\"A\":\"记录错因并重新说明解题依据\",\"B\":\"只统计做题数量\",\"C\":\"跳过解析\",\"D\":\"把错误都归为粗心\"}'
        WHEN 5 THEN '{\"A\":\"检查题意和条件\",\"B\":\"核对关键步骤\",\"C\":\"对照解析修正误区\",\"D\":\"不看单位、条件或逻辑\"}'
        WHEN 6 THEN '{\"A\":\"正确\",\"B\":\"错误\"}'
        ELSE '{\"A\":\"读题明确目标 → 提取条件 → 选择方法 → 检验结果\",\"B\":\"先猜答案 → 再找理由\",\"C\":\"只看最后一句 → 直接提交\",\"D\":\"忽略条件 → 套用任意结论\"}'
    END AS options,
    CASE t.seq
        WHEN 1 THEN 'A'
        WHEN 2 THEN 'A,B,C'
        WHEN 3 THEN 'B'
        WHEN 4 THEN 'A'
        WHEN 5 THEN 'A,B,C'
        WHEN 6 THEN 'A'
        ELSE 'A'
    END AS answer,
    CASE t.seq
        WHEN 1 THEN CONCAT('学习“', nq.node_name, '”前，应先明确学习目标、已有基础和关键概念。')
        WHEN 2 THEN CONCAT('“', nq.node_name, '”需要概念、条件和练习共同支撑，不能只背答案。')
        WHEN 3 THEN '结论必须结合条件和过程理解，否则遇到变式题容易误用。'
        WHEN 4 THEN '有效复盘要能说清错因、依据和修正方法，而不是只统计数量。'
        WHEN 5 THEN '检查题意、步骤和解析能帮助发现理解漏洞，降低重复出错。'
        WHEN 6 THEN '错题复盘的重点是修正理解，而不是只看是否答对。'
        ELSE '综合题应先明确目标和条件，再选择方法，最后检验结果是否合理。'
    END AS analysis,
    CASE t.seq
        WHEN 2 THEN 2
        WHEN 5 THEN 2
        WHEN 7 THEN 2
        ELSE 1
    END AS difficulty,
    CASE t.seq
        WHEN 2 THEN 'multi'
        WHEN 3 THEN 'judge'
        WHEN 5 THEN 'multi'
        WHEN 6 THEN 'judge'
        ELSE 'single'
    END AS question_type,
    NOW() AS create_time
FROM (
    SELECT kn.id AS node_id, kn.name AS node_name, COUNT(q.id) AS question_count
    FROM knowledge_node kn
    LEFT JOIN question q ON q.node_id = kn.id
    GROUP BY kn.id, kn.name
) nq
JOIN (
    SELECT 1 AS seq UNION ALL
    SELECT 2 UNION ALL
    SELECT 3 UNION ALL
    SELECT 4 UNION ALL
    SELECT 5 UNION ALL
    SELECT 6 UNION ALL
    SELECT 7
) t ON t.seq > nq.question_count AND t.seq <= 7
WHERE NOT EXISTS (
    SELECT 1
    FROM question existing
    WHERE existing.node_id = nq.node_id
      AND existing.content = CASE t.seq
          WHEN 1 THEN CONCAT('围绕“', nq.node_name, '”，学习前最应该先确认什么？')
          WHEN 2 THEN CONCAT('学习“', nq.node_name, '”时，哪些做法能帮助形成稳定理解？')
          WHEN 3 THEN CONCAT('判断：只要背下“', nq.node_name, '”的结论，不理解条件也能稳定迁移应用。')
          WHEN 4 THEN CONCAT('关于“', nq.node_name, '”的学习过程，下列哪项最能体现有效复盘？')
          WHEN 5 THEN CONCAT('在解决“', nq.node_name, '”相关问题时，哪些检查步骤是合理的？')
          WHEN 6 THEN CONCAT('判断：完成“', nq.node_name, '”练习后，应结合错因和解析修正自己的理解。')
          ELSE CONCAT('遇到“', nq.node_name, '”综合应用题时，比较稳妥的处理顺序是什么？')
      END
);

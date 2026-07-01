USE kg_tutoring_db;

-- 1. 邮箱验证码表（无依赖）
CREATE TABLE IF NOT EXISTS sys_email_code (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    email VARCHAR(50) NOT NULL COMMENT '目标邮箱',
    code VARCHAR(10) NOT NULL COMMENT '6位验证码',
    type VARCHAR(20) NOT NULL COMMENT '类型：register注册 reset找回密码',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    is_used TINYINT DEFAULT 0 COMMENT '是否已使用：0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_email_type (email, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证码表';

-- 2. 课程表（依赖 sys_user）
CREATE TABLE IF NOT EXISTS course (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '课程主键ID',
    subject VARCHAR(30) NOT NULL COMMENT '所属学科',
    course_name VARCHAR(50) NOT NULL COMMENT '课程名称',
    description TEXT DEFAULT NULL COMMENT '课程简介',
    teacher_id INT DEFAULT NULL COMMENT '负责教师ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 3. 知识点节点表（依赖 course）
CREATE TABLE IF NOT EXISTS knowledge_node (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '知识点主键ID',
    course_id INT NOT NULL COMMENT '所属课程ID',
    name VARCHAR(50) NOT NULL COMMENT '知识点名称',
    description TEXT DEFAULT NULL COMMENT '知识点讲解内容',
    difficulty TINYINT DEFAULT 1 COMMENT '难度：1基础 2进阶 3困难',
    chapter VARCHAR(50) DEFAULT NULL COMMENT '所属章节',
    expected_minutes INT DEFAULT 30 COMMENT '预计学习时长(分钟)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点节点表';

-- 4. 知识点依赖边表（依赖 knowledge_node）
CREATE TABLE IF NOT EXISTS knowledge_edge (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '边主键ID',
    from_node_id INT NOT NULL COMMENT '前置知识点ID',
    to_node_id INT NOT NULL COMMENT '后继知识点ID',
    relation_type VARCHAR(20) DEFAULT 'prerequisite' COMMENT '关系类型',
    is_cross_subject TINYINT DEFAULT 0 COMMENT '是否跨学科：0否 1是',
    UNIQUE KEY uk_from_to (from_node_id, to_node_id),
    FOREIGN KEY (from_node_id) REFERENCES knowledge_node(id),
    FOREIGN KEY (to_node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点依赖边表';

-- 5. 习题库表（依赖 knowledge_node）
CREATE TABLE IF NOT EXISTS question (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '题目主键ID',
    node_id INT NOT NULL COMMENT '关联知识点ID',
    content TEXT NOT NULL COMMENT '题干',
    options TEXT DEFAULT NULL COMMENT '选项(JSON格式)',
    answer VARCHAR(200) NOT NULL COMMENT '标准答案',
    analysis TEXT DEFAULT NULL COMMENT '题目解析',
    difficulty TINYINT DEFAULT 1 COMMENT '难度等级',
    question_type VARCHAR(20) DEFAULT 'single' COMMENT '题型：single/multi/judge',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='习题库表';

-- 6. 学习记录表（依赖 sys_user + knowledge_node）
CREATE TABLE IF NOT EXISTS study_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录主键ID',
    user_id INT NOT NULL COMMENT '学生ID',
    node_id INT NOT NULL COMMENT '知识点ID',
    mastery_level TINYINT DEFAULT 0 COMMENT '掌握度：0未学 1学习中 2已掌握',
    correct_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '答题正确率',
    study_minutes INT DEFAULT 0 COMMENT '累计学习时长(分钟)',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_node (user_id, node_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点学习记录表';

-- 7. 学习路径主表（依赖 sys_user + knowledge_node）
CREATE TABLE IF NOT EXISTS study_path (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '路径主键ID',
    user_id INT NOT NULL COMMENT '学生ID',
    target_node_id INT NOT NULL COMMENT '目标知识点ID',
    path_name VARCHAR(100) DEFAULT NULL COMMENT '路径名称',
    total_nodes INT DEFAULT NULL COMMENT '总节点数',
    total_minutes INT DEFAULT NULL COMMENT '总预计时长(分钟)',
    ai_summary TEXT DEFAULT NULL COMMENT 'AI路径总览',
    status TINYINT DEFAULT 0 COMMENT '状态：0进行中 1已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (target_node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径主表';

-- 8. 学习路径详情表（依赖 study_path + knowledge_node）
CREATE TABLE IF NOT EXISTS path_detail (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '详情主键ID',
    path_id INT NOT NULL COMMENT '所属路径ID',
    node_id INT NOT NULL COMMENT '知识点ID',
    sort_order INT NOT NULL COMMENT '学习顺序号',
    is_finished TINYINT DEFAULT 0 COMMENT '是否完成：0否 1是',
    FOREIGN KEY (path_id) REFERENCES study_path(id),
    FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径详情表';

-- 9. 测评记录表（依赖 sys_user + course）
CREATE TABLE IF NOT EXISTS exam_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '测评记录ID',
    user_id INT NOT NULL COMMENT '学生ID',
    course_id INT DEFAULT NULL COMMENT '所属课程ID',
    total_score INT DEFAULT NULL COMMENT '试卷总分',
    user_score DECIMAL(5,2) DEFAULT NULL COMMENT '学生得分',
    ai_report TEXT DEFAULT NULL COMMENT 'AI诊断报告',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (course_id) REFERENCES course(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评记录表';

-- 10. 错题本表（依赖 sys_user + question）
CREATE TABLE IF NOT EXISTS wrong_question (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '错题ID',
    user_id INT NOT NULL COMMENT '学生ID',
    question_id INT NOT NULL COMMENT '题目ID',
    wrong_answer VARCHAR(200) DEFAULT NULL COMMENT '错误答案',
    wrong_count INT DEFAULT 1 COMMENT '错误次数',
    ai_explain TEXT DEFAULT NULL COMMENT 'AI错题讲解',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次错误时间',
    UNIQUE KEY uk_user_question (user_id, question_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (question_id) REFERENCES question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

-- 11. 跨学科主题表（依赖 sys_user）
CREATE TABLE IF NOT EXISTS cross_subject_theme (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主题主键ID',
    theme_name VARCHAR(100) NOT NULL COMMENT '主题名称',
    description TEXT DEFAULT NULL COMMENT '主题背景与目标',
    difficulty TINYINT DEFAULT 2 COMMENT '主题难度',
    publisher_id INT DEFAULT NULL COMMENT '发布教师ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0下架 1发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跨学科主题表';

-- 12. 主题-知识点关联表（依赖 cross_subject_theme + knowledge_node）
CREATE TABLE IF NOT EXISTS cross_theme_node (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录ID',
    theme_id INT NOT NULL COMMENT '主题ID',
    node_id INT NOT NULL COMMENT '知识点ID',
    UNIQUE KEY uk_theme_node (theme_id, node_id),
    FOREIGN KEY (theme_id) REFERENCES cross_subject_theme(id),
    FOREIGN KEY (node_id) REFERENCES knowledge_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主题-知识点关联表';

-- 13. 系统公告表（依赖 sys_user）
CREATE TABLE IF NOT EXISTS sys_notice (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(100) NOT NULL COMMENT '公告标题',
    content TEXT DEFAULT NULL COMMENT '公告正文',
    publisher_id INT DEFAULT NULL COMMENT '发布人ID',
    target_role VARCHAR(20) DEFAULT 'all' COMMENT '推送对象：all/student/teacher',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 14. 系统操作日志表（无强外键，兼容用户删除场景）
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id INT DEFAULT NULL COMMENT '操作人ID',
    module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    operation VARCHAR(200) DEFAULT NULL COMMENT '操作内容',
    ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- 15. AI调用日志表（无强外键）
CREATE TABLE IF NOT EXISTS ai_call_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id INT DEFAULT NULL COMMENT '调用用户ID',
    scene VARCHAR(50) DEFAULT NULL COMMENT '调用场景',
    prompt TEXT DEFAULT NULL COMMENT '输入提示词',
    result TEXT DEFAULT NULL COMMENT 'AI返回结果',
    call_duration INT DEFAULT NULL COMMENT '调用耗时(ms)',
    status TINYINT DEFAULT 1 COMMENT '状态：1成功 0失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    INDEX idx_user_id (user_id),
    INDEX idx_scene (scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';
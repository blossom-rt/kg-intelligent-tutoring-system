USE kg_tutoring_db;

-- 1. 创建系统角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色主键ID',
    role_code VARCHAR(30) NOT NULL UNIQUE COMMENT '角色唯一标识：admin/teacher/student',
    role_name VARCHAR(30) NOT NULL COMMENT '角色显示名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述与权限范围'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 2. 初始化3个基础角色
INSERT INTO sys_role (role_code, role_name, description) VALUES
('admin', '系统管理员', '系统最高权限，负责运维与用户管理'),
('teacher', '教师', '负责课程、知识点、题库管理与学情跟踪'),
('student', '学生', '自主学习、刷题测评、获取个性化路径');
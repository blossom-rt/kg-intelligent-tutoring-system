-- 初始化一个用户表脚本用于测试

CREATE DATABASE IF NOT EXISTS kg_tutoring_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE kg_tutoring_db;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id       INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    role     VARCHAR(20)  NOT NULL COMMENT '角色：student学生 / teacher教师'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 测试数据
INSERT INTO sys_user (username, password, role) VALUES
('student', '123456', 'student'),
('teacher', '123456', 'teacher');

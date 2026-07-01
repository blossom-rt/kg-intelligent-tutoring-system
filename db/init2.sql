USE kg_tutoring_db;

-- 1. 新增字段（允许为空，兼容现有数据）
ALTER TABLE sys_user
ADD COLUMN real_name VARCHAR(30) DEFAULT NULL COMMENT '真实姓名' AFTER username,
ADD COLUMN email VARCHAR(50) DEFAULT NULL COMMENT '绑定邮箱' AFTER real_name,
ADD COLUMN role_id INT DEFAULT NULL COMMENT '关联角色ID' AFTER email,
ADD COLUMN status TINYINT DEFAULT 1 COMMENT '账号状态：1正常 0禁用' AFTER role_id,
ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER status,
ADD UNIQUE INDEX uk_email (email),
ADD INDEX idx_role_id (role_id);

-- 2. 根据旧的 role 字段，给现有账号匹配角色ID
UPDATE sys_user SET role_id = 3 WHERE role = 'student'; -- 学生对应id=3
UPDATE sys_user SET role_id = 2 WHERE role = 'teacher'; -- 教师对应id=2

-- 3. 将明文密码更新为MD5加密（123456的MD5值）
UPDATE sys_user SET password = 'e10adc3949ba59abbe56e057f20f883e';

-- 4. 设置 role_id 非空，添加外键约束
ALTER TABLE sys_user
MODIFY COLUMN role_id INT NOT NULL COMMENT '关联角色ID',
ADD FOREIGN KEY (role_id) REFERENCES sys_role(id);
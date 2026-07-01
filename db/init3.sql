-- 5. 删除旧的 role 字符串字段，消除冗余
ALTER TABLE sys_user DROP COLUMN role;
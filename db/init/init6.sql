USE kg_tutoring_db;
CREATE TABLE IF NOT EXISTS user_favorite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    node_id INT NOT NULL COMMENT '知识点ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_node (user_id, node_id)
) COMMENT '用户收藏表';
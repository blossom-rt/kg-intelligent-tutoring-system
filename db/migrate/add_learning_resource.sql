USE kg_tutoring_db;

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

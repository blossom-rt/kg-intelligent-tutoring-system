# 技术架构图 — ChatGPT 绘图提示词

请用 DOT (Graphviz) 绘制一张技术架构图，严格按照以下规范。

## 布局结构

从上到下分 6 层，每层一个浅色大圆角矩形框。左侧放彩色竖条标签，右侧大框内横向排列小圆角矩形。

## 6 层内容

### 第1层：访问层（绿色系）
- 标签：`访问层`
- 标签颜色：`fillcolor="#43A047" fontcolor=white`
- 大框：`fillcolor="#E8F5E9" color="#A5D6A7"`
- 内部小框：`fillcolor="#C8E6C9" color="#66BB6A" fontcolor="#1B5E20"`
- 内容：PC 浏览器、手机浏览器、IntelliJ IDEA

### 第2层：前端框架（粉红色系）
- 标签：`前端框架`
- 标签颜色：`fillcolor="#C62828" fontcolor=white`
- 大框：`fillcolor="#FCE4EC" color="#F48FB1"`
- 内部小框：`fillcolor="#F8BBD0" color="#E57373" fontcolor="#880E4F"`
- 内容：Vue 3、Vite 8、Element Plus 2、ECharts 5、Pinia、Vue Router 4、Axios

### 第3层：功能模块（蓝色系）
- 标签：`功能模块`
- 标签颜色：`fillcolor="#1565C0" fontcolor=white`
- 大框：`fillcolor="#E3F2FD" color="#90CAF9"`
- 内部小框：`fillcolor="#BBDEFB" color="#64B5F6" fontcolor="#0D47A1"`
- 内容：学生端 15个页面、教师端 10个页面、管理员端 7个页面、认证页 3个页面

### 第4层：业务逻辑（紫色系）
- 标签：`业务逻辑`
- 标签颜色：`fillcolor="#4527A0" fontcolor=white`
- 大框：`fillcolor="#EDE7F6" color="#B39DDB"`
- 内部小框：`fillcolor="#D1C4E9" color="#7E57C2" fontcolor="#311B92"`
- 内容：知识图谱(拓扑排序)、学习路径(自动生成)、在线刷题(即时判分)、测评中心(AI诊断)、错题本(AI讲解)、跨学科(主题融合)、学情分析(数据统计)、学习伙伴(CSS动画)

### 第5层：中间件/服务层（橙黄色系）
- 标签：`中间件/服务层`
- 标签颜色：`fillcolor="#E65100" fontcolor=white`
- 大框：`fillcolor="#FFF3E0" color="#FFCC80"`
- 内部小框：`fillcolor="#FFE0B2" color="#FF9800" fontcolor="#BF360C"`
- 内容：Spring Boot 3.5、MyBatis-Plus、JWT 鉴权、JavaMail、AOP 日志、Maven (阿里云镜像)

### 第6层：数据库/基础设施（红色系）
- 标签：`数据库/基础设施`
- 标签颜色：`fillcolor="#B71C1C" fontcolor=white`
- 大框：`fillcolor="#FFEBEE" color="#EF9A9A"`
- 内部小框：`fillcolor="#FFCDD2" color="#E57373" fontcolor="#B71C1C"`
- 内容：MySQL 8.0 (19张表)、Docker 容器化、init/seed/migrate 数据脚本

## 画法规范

1. `rankdir=TB; splines=ortho; compound=true; newrank=true;`
2. 标签节点：`shape=box, style="filled,bold"`，`\n` 竖排文字，`fixedsize=true`，宽 0.55，高 0.85
3. 内容节点：`shape=box, style="rounded,filled"`，圆角矩形，宽 1.3，高 0.55
4. 每层用 `subgraph cluster_X { }` 包裹内容节点，`label=""` 不显示标题
5. 标签节点放在 cluster 外面左侧，用 `{ rank=same; 标签; 内容首节点; }` 对齐
6. 用不可见占位节点 `[style=invis]` 使各层宽度对齐
7. 全局字体 Times-Roman，标题 16pt
8. `edge [style=invis]` 从 L1→L2→L3→L4→L5→L6 强制标签列垂直对齐

# 知径 — 面向课程教学的智能导学系统

基于知识图谱的智能导学系统，实现「知识组织 → 路径生成 → 学习执行 → 测评反馈 → 薄弱点诊断」的完整教学闭环。

---

## 技术栈

| 层 | 技术 |
|------|---------|
| 后端框架 | Spring Boot 3.5.16 + MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0.46 + HikariCP 连接池 |
| 认证 | JWT + MD5 密码加密 |
| 前端框架 | Vue 3.5 + Vite 8 + Element Plus 2.14 |
| 可视化 | Three.js（3D 图谱）、ECharts、D3 |
| AI | DeepSeek API |
| 部署 | Docker Compose |

---

## 功能总览

### 学生端

| 功能 | 说明 |
|------|------|
| 知识图谱 | 3D 可视化图谱，支持按课程/章节筛选，节点按章节分色 |
| 学习路径 | 系统按拓扑排序自动生成，按章节分组展示，显示章节完成进度 |
| 知识点学习 | 图文学习 + AI 划重点 + AI 答疑 + 配套练习 |
| 在线练习 | 逐题作答，自动批改 |
| 在线测评 | 计时作答，自动判分，AI 生成学情诊断报告 |
| 错题本 | 自动收录错题，支持 AI 错题讲解 |
| 收藏夹 | 收藏重点知识点 |
| 前置知识回顾 | 学习时自动展示前置知识点卡片 |
| 思维导图导出 | 学习路径导出为 PNG 树形图 |
| 个性化推荐 | 学这门课的同学还学 / 相关课程推荐 |

### 教师端

| 功能 | 说明 |
|------|------|
| 课程管理 | 维护课程基本信息 |
| 章节管理 | 课程章节增删改查，拖拽排序，教学目标填写 |
| 知识点管理 | 课程→章节→知识点三级级联，配置学习资源 |
| 图谱编辑 | 可视化编辑知识点依赖关系 |
| 题库管理 | 题目的增删改查 |
| 测评管理 | 创建测评，智能选题（覆盖所有知识点） |
| 学情分析 | 班级掌握度分布、知识点正确率排行、章节掌握度、薄弱点诊断 |
| 学习路径督导 | 查看每个学生的学习路径完成进度 |

### 管理员端

| 功能 | 说明 |
|------|------|
| 用户管理 | 学生/教师账号管理，启用/禁用 |
| 角色管理 | 维护系统角色 |
| 课程管理 | 全平台课程维护 |
| 公告管理 | 发布、编辑、删除系统公告 |
| 操作日志 | 全模块操作审计日志 |
| AI 调用监控 | 查看 AI 调用记录与状态 |

---

## 数据库（22 张表）

| 分类 | 表 | 说明 |
|------|----|------|
| 用户体系 | `sys_role` | 角色 |
| | `sys_user` | 用户 |
| | `sys_email_code` | 邮箱验证码 |
| 知识图谱 | `course` | 课程 |
| | `chapter` | 章节（支持排序） |
| | `knowledge_node` | 知识点（关联章节） |
| | `learning_resource` | 学习资源 |
| | `knowledge_edge` | 前置依赖边 |
| | `user_favorite` | 用户收藏 |
| 学习与练习 | `question` | 习题 |
| | `study_record` | 学习记录 |
| | `study_path` | 学习路径 |
| | `path_detail` | 路径详情 |
| 测评 | `exam` | 测评定义 |
| | `exam_question` | 测评-题目关联 |
| | `exam_record` | 测评记录 |
| | `wrong_question` | 错题本 |
| 跨学科 | `cross_subject_theme` | 跨学科主题 |
| | `cross_theme_node` | 主题-知识点关联 |
| 系统运维 | `sys_notice` | 系统公告 |
| | `sys_oper_log` | 操作日志 |
| | `ai_call_log` | AI 调用日志 |

---

## 快速启动

### 数据库

数据库初始化脚本及相关说明见 [`db/README.md`](db/README.md)。

### 后端

```bash
cd kg-tutoring-backend
cp src/main/resources/application-template.properties src/main/resources/application.properties
# 编辑 application.properties，需配置以下内容：
# - spring.datasource.password          数据库密码
# - deepseek.api.key                    你的 DeepSeek API Key（AI 功能需要）
```

配置完成后启动：

```bash
./mvnw spring-boot:run
```

### 前端

```bash
cd kg-tutoring-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`。

### Docker Compose

```bash
docker compose up -d --build
```

访问 `http://localhost`。

---

## 项目结构

```
kg-tutoring-backend/         Spring Boot 3 后端
  src/main/java/com/cupk/
    controller/              REST 控制器
    service/                 业务逻辑
    mapper/                  MyBatis-Plus Mapper
    pojo/                    数据库实体
    ai/                      DeepSeek AI 服务
    config/                  JWT 鉴权、跨域配置
    aspect/                  AOP 操作日志

kg-tutoring-frontend/        Vue 3 前端
  src/views/student/         学生端页面
  src/views/teacher/         教师端页面
  src/views/admin/           管理员端页面
  src/api/                   后端 API 封装
  src/router/                路由配置

db/                          数据库初始化脚本
```

---

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `student` | `123456` | 学生 |
| `teacher` | `123456` | 教师 |
| `admin` | `admin123` | 管理员 |

更多演示教师和学生账号见 [`db/README.md`](db/README.md)。

---

## 用户流程

### 学生
```
注册 → 浏览知识图谱 → 选择目标知识点 → 生成学习路径
→ 逐节点学习（学内容 → 做练习 → 达标 → 解锁下一节点）
→ 参加测评 → 查看 AI 诊断报告 → 错题巩固
```

### 教师
```
维护知识点 → 建立依赖关系 → 管理题库 → 发布测评
→ 查看学情数据 → 督导学生路径
```

### 管理员
```
管理用户账号 → 配置角色 → 课程维护 → 发布公告 → 日志审计
```

---

## 文档索引

| 文档 | 用途 |
|---|---|
| [接口文档](docs/api.md) | 后端接口清单 |
| [数据库说明](db/README.md) | 初始化、迁移、重置说明 |
| [开发与维护指南](docs/开发与维护指南.md) | 配置、联调、排错 |

---

## 致谢

此项目由 sjh、sxy、ls 倾尽十天心血制作，谨在此纪念我们能在 20 岁的年纪仍在努力。革命尚未成功，同志仍需努力。这一杯，敬自由。 🍻

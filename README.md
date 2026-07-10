# 知径——面向课程教学的智能导学系统

基于知识图谱的智能导学系统，服务于课程教学场景。支持学生自主学习、教师管理教学内容、管理员运维审计。

---

## 项目结构

```
kg-tutoring-backend/    ← Spring Boot 3 后端（Java 21）
kg-tutoring-frontend/   ← Vue 3 前端
db/                     ← 数据库初始化脚本（按顺序执行）
```

---

## 技术栈

| 层 | 技术 |
|------|---------|
| 后端框架 | Spring Boot 3.5.16 + MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 + HikariCP 连接池 |
| 认证 | JWT + MD5 密码加密 |
| 前端框架 | Vue 3.5 + Vite 8 + Element Plus 2.14 |
| 状态管理 | Pinia |
| 路由 | Vue Router 5.1 |
| 可视化 | ECharts 6.1、D3、Three.js |

---

## 已实现功能

### 通用能力

- 基于 JWT 的登录鉴权与前端路由，按 `student`、`teacher`、`admin` 三类角色进入不同工作台。
- 支持学生自主注册、邮箱验证码发送、邮箱找回密码、登录后修改密码。
- 支持个人资料查看与修改，统一封装接口响应、异常处理、跨域配置和请求拦截。
- 后端通过操作日志切面记录关键管理操作，AI 接口调用会写入调用日志，便于管理员审计。

### 学生端

- 学生首页仪表盘：汇总学习进度、路径、测评、错题等核心学习数据。
- 知识图谱浏览：按课程查看知识点节点与前置依赖关系，支持图谱化呈现学习内容。
- 学习路径生成：可按目标知识点生成路径，也可按跨学科主题生成路径；支持查看路径详情、更新节点学习状态、删除路径。
- 知识点学习：展示知识点说明、学习材料、重点难点、学习建议和配套视频资源，并记录掌握情况。
- 在线练习：按知识点获取练习题，提交答案后更新练习结果和学习记录。
- 在线测评：查看可参加测评、加载试卷、提交测评答案、查看成绩与测评结果。
- 错题本：自动沉淀练习和测评中的错题，支持按条件查看、手动添加和删除错题。
- 个性化分析：查看个人学习分析、薄弱知识点分析和学习建议。
- 跨学科主题学习：浏览跨学科主题，查看主题关联知识点，并基于主题进入学习路径。
- AI 学习助手：支持知识点总结、错题讲解、测评报告生成和学习问答对话。

### 教师端

- 教师首页仪表盘：展示课程、知识点、题目、测评和学生学习情况概览。
- 课程管理：维护教师负责课程的基础信息。
- 知识点管理：新增、编辑、删除知识点，维护学习材料、难度、预计学习时间、重点难点等字段，并可为知识点配置视频/文章/文档等学习资源。
- 图谱依赖管理：维护知识点之间的前置依赖边，支撑学生端知识图谱与路径生成。
- 题库管理：维护单选、多选、判断、简答等题目，关联课程与知识点，配置答案、解析和难度。
- 测评管理：创建、编辑、删除测评，配置测评题目、分值、时长和发布状态。
- 跨学科主题管理：维护主题信息、关联知识点，并控制主题启用状态。
- 学情分析：查看班级学习统计、学生列表、学生学习趋势和学习路径完成情况。
- 学习路径督导：按学生查看学习路径与节点完成状态，辅助教师跟进学习过程。

### 管理员端

- 管理员首页：提供系统运维入口与关键数据概览。
- 用户管理：支持用户查询、新增、编辑、删除和启用/禁用。
- 角色管理：维护系统角色信息。
- 课程管理：管理员可统一维护课程数据。
- 公告管理：发布、编辑、删除系统公告，供前端展示。
- 操作日志审计：查看系统关键操作记录，支持运维追踪。
- AI 调用监控：查看 AI 调用日志、调用场景、用户、耗时和结果状态。

### 数据与部署

- 提供 20 张核心业务表，覆盖用户角色、知识图谱、学习资源、学习记录、学习路径、测评、错题、跨学科主题、公告、操作日志和 AI 调用日志。
- 提供完整建库脚本、演示数据脚本、学习材料补充脚本，以及旧库迁移和业务数据清理脚本。
- 提供前后端 Dockerfile 与 `docker-compose.yml`，支持 MySQL、后端、前端容器化部署。
- 项目文档包含接口文档、图表说明、ER 图、类图、活动图、时序图和通信图等设计资料。

---

## 数据库（20 张表）

```
-- 用户与角色
sys_role              ← 角色（admin / teacher / student）
sys_user              ← 用户（关联角色）
sys_email_code        ← 邮箱验证码（注册/找回密码）

-- 知识图谱
course                ← 课程（关联教师）
knowledge_node        ← 知识点节点（图谱顶点）
learning_resource     ← 学习资源（视频/文章/文档/链接）
knowledge_edge        ← 前置依赖边（图谱有向边）

-- 学习与练习
question              ← 习题（关联知识点）
study_record          ← 学习记录（学生→知识点，掌握度）
study_path            ← 学习路径
path_detail           ← 路径详情（排序后的知识点序列）

-- 测评
exam                  ← 测评定义
exam_question         ← 测评-题目关联
exam_record           ← 测评记录
wrong_question        ← 错题本

-- 跨学科主题
cross_subject_theme   ← 跨学科主题
cross_theme_node      ← 主题-知识点关联

-- 系统运维
sys_notice            ← 系统公告
sys_oper_log          ← 操作日志
ai_call_log           ← AI 调用日志
```

---

## 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 21 |
| Maven | 3.9.10 |
| Node.js | `^22.18.0` 或 `>=24.12.0` |
| MySQL | 8.0 |

---

## 快速启动

### 1. 数据库

全新环境，一条命令搞定：

```bash
mysql -u root -p < db/init_full.sql
```

也可以分步执行（效果完全相同）：

```bash
mysql -u root -p < db/init/init_all.sql                    # 建库建表
mysql -u root -p < db/init/seed_all.sql                     # 导入演示数据
mysql -u root -p < db/init/enrich_learning_materials.sql    # 补充学习资料
```

如果只有旧库缺少 `exam`、`exam_question` 表（7 月 2 日前建的库）：

```bash
mysql -u root -p < db/migrate/migrate_exam.sql
```

如果测试过程中数据乱了，想重置：

```bash
mysql -u root -p < db/migrate/cleanup.sql      # 清空业务数据（保留 admin/teacher/student）
mysql -u root -p < db/init/seed_all.sql
mysql -u root -p < db/init/enrich_learning_materials.sql
```

详细脚本说明见 [`db/README.md`](db/README.md)。

### 2. 后端

```bash
# 复制配置模板，填入你自己的数据库密码
cp kg-tutoring-backend/src/main/resources/application-template.properties kg-tutoring-backend/src/main/resources/application.properties

# 启动（默认端口 8080）
cd kg-tutoring-backend
./mvnw spring-boot:run
```

### 3. 前端

```bash
cd kg-tutoring-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求通过 Vite 代理转发到后端。

### 4. Docker Compose（可选）

```bash
docker compose up -d --build
```

启动后访问 `http://localhost`；MySQL 映射到宿主机 `3307`，后端映射到 `8080`。
首次启动会自动导入演示数据。若需要重新初始化，请先执行 `docker compose down -v`，这会删除 Docker 数据卷中的数据库数据。

> `docker-compose.yml` 中的数据库密码仅供本地演示。部署到共享环境前，请改用环境变量或未提交的 `.env` 文件，并同时替换 JWT 密钥和 DeepSeek API Key。

---

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `student` | `123456` | 学生 |
| `teacher` | `123456` | 教师 |
| `admin` | `admin123` | 管理员 |

---

## 文档索引

| 文档 | 用途 |
|---|---|
| [开发与维护指南](docs/开发与维护指南.md) | 目录职责、配置项、联调路径与常见排查 |
| [接口文档](docs/api.md) | 当前后端 Controller 与前端 API 调用的接口清单 |
| [数据库说明](db/README.md) | 初始化、重置、迁移及演示数据说明 |
| [系统功能架构](docs/系统功能架构.md) | 业务模块和分层架构说明 |
| [图表说明](docs/diagrams.md) | UML 图及其源文件说明 |

接口文档以 Controller 为准；前端 `src/api` 中尚未落地后端的调用会在文档中明确标注。

---

## 用户流程

### 学生
```
注册 → 登录 → 浏览知识图谱 → 选目标知识点
         → 生成学习路径 → 阶梯式学习（学→刷题→达标→解锁）
         → 测评论证 → 错题巩固 → AI 薄弱点推荐
```

### 教师
```
维护知识点与依赖关系 → 管理题库 → 设计跨学科主题
→ 发布测评 → 查看学情数据 → 督导学习路径
```

### 管理员
```
管理用户账号 → 配置角色权限 → 审核内容
→ 系统公告 → 操作日志审计 → AI 调用监控
```

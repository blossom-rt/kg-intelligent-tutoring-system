# 面向课程教学的知识图谱智能导学系统

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
| 后端框架 | Spring Boot 3.5 + MyBatis Plus 3.5 |
| 数据库 | MySQL 8.0 + HikariCP 连接池 |
| 认证 | JWT (jjwt 0.11.5) + MD5 密码加密 |
| 前端框架 | Vue 3.5 + Vite + Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router 5 |

---

## 数据库（19 张表）

```
sys_role              ← 角色（admin / teacher / student）
sys_user              ← 用户（关联角色）

course                ← 课程（关联教师）
knowledge_node        ← 知识点节点（图谱顶点）
knowledge_edge        ← 前置依赖边（图谱有向边）

question              ← 习题（关联知识点）
study_record          ← 学习记录（学生→知识点，掌握度）
study_path            ← 学习路径
path_detail           ← 路径详情（排序后的知识点序列）

exam                  ← 测评定义
exam_question         ← 测评-题目关联
exam_record           ← 测评记录
wrong_question        ← 错题本
cross_subject_theme   ← 跨学科主题
cross_theme_node      ← 主题-知识点关联

sys_email_code        ← 邮箱验证码
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
| Node.js | v24.14.0 |
| MySQL | 8.0.46 |

---

## 快速启动

### 1. 数据库

按顺序执行初始化脚本：

```bash
mysql -u root -p < db/init_all.sql
mysql -u root -p < db/seed_all.sql
```

如果是已经初始化过的旧库，只需要补测评定义表，可执行：

```bash
mysql -u root -p < db/migrate_exam.sql
```

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

---

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `student` | `123456` | 学生 |
| `teacher` | `123456` | 教师 |
| `admin` | `admin123` | 管理员 |

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

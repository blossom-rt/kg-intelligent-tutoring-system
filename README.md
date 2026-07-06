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
| 认证 | JWT + MD5 密码加密 |
| 前端框架 | Vue 3.5 + Vite + Element Plus 2.x |
| 状态管理 | Pinia |
| 路由 | Vue Router 5 |
| 可视化 | ECharts 6.x |

---

## 数据库（21 张表）

```
-- 用户与角色
sys_role              ← 角色（admin / teacher / student）
sys_user              ← 用户（关联角色）
sys_email_code        ← 邮箱验证码（注册/找回密码）

-- 知识图谱
course                ← 课程（关联教师）
knowledge_node        ← 知识点节点（图谱顶点）
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
| Node.js | v24.14.0 |
| MySQL | 8.0.46 |

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

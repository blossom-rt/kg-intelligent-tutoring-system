# 面向课程教学的知识图谱智能导学系统

基于知识图谱的智能导学系统，服务于课程教学场景。

---

## 项目结构

```
kg-tutoring-backend/    ← Spring Boot 3 后端
kg-tutoring-frontend/   ← Vue 3 前端
db/                     ← 数据库脚本
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

```bash
# 创建数据库和表
mysql -u root -p < db/init.sql
```

### 2. 后端

```bash
# 复制配置模板，填入自己的数据库密码
cp kg-tutoring-backend/src/main/resources/application-template.properties kg-tutoring-backend/src/main/resources/application.properties

# 启动后端（默认端口 8080）
cd kg-tutoring-backend
./mvnw spring-boot:run
```

### 3. 前端

```bash
cd kg-tutoring-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，API 请求会自动代理到后端。

---

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `student` | `123456` | 学生 |
| `teacher` | `123456` | 教师 |

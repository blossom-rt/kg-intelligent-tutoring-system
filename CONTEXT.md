# 项目上下文 — 给下一个 Agent

## 项目概览

KG 智能导学系统 — 基于知识图谱的大学生在线教育平台。CUPK · 计算机系。

- 仓库：`https://github.com/blossom-rt/kg-intelligent-tutoring-system`
- 分支：`dev`
- 本地路径：`/Users/lishuai/univ./sophomore/summer_semester/kg-intelligent-tutoring-system/`

## 技术栈

- **前端**：Vue 3 + Vite + Element Plus 2.x + ECharts 5.x + Pinia + Vue Router 4 + Axios
- **后端**：Spring Boot 3.5 + MyBatis-Plus + MySQL 8.0 + JWT
- **端口**：前端 5173，后端 8080
- **数据库**：`kg_tutoring_db`，用户 root，密码 983491037

## 启动命令

```bash
# 前端
cd kg-tutoring-frontend && npm run dev

# 后端
cd kg-tutoring-backend && mvn spring-boot:run -q
```

## 测试账号

student/123456  teacher/123456  admin/admin123

## 设计规范

暖奶油底 `#faf7f2` + 暖橙强调 `#ff7b3d`，禁用紫色/靛蓝/ease-in-out。
全局样式 `src/styles/global.css`，详细规范见 `AGENTS.md`。

## 关键组件

| 组件 | 位置 | 用途 |
|---|---|---|
| Sidebar | `src/components/` | 学生/教师端左侧导航，可折叠（useSidebar.js），管理员不用 |
| StudentHeader | `src/components/` | 统一页面头部，返回按角色跳首页 |
| StudyPet | `src/components/` | 右下角学习宠物，答对庆祝/答错安慰 |
| AnimatedCharacters | `src/components/` | 登录页角色插画 |

## 后端重要修复

- `StudentController/TeacherController` 已加 `/api` 前缀
- `StudentWrongController` 有 POST 错题收录接口
- Dashboard 进度从 `study_record.masteryLevel` 计算
- Vite 代理：`/api/kg` → `/api/*`

## Git

- 作者：`hahedaftyudu-ly` / `hahedaftyudu@gmail.com`
- 不要出现 Claude/Co-Authored-By
- Maven 镜像：`~/.m2/settings.xml` 阿里云

# 项目上下文 — 给下一个 Agent

## 项目概览

**KG 智能导学系统** — 基于知识图谱的大学生在线教育平台。CUPK · 计算机系。

- 仓库：`https://github.com/blossom-rt/kg-intelligent-tutoring-system`
- 分支：`dev`
- 本地路径：`/Users/lishuai/univ./sophomore/summer_semester/kg-intelligent-tutoring-system/`

## 技术栈

- **前端**：Vue 3 + Vite + Element Plus 2.x + ECharts 5.x + Pinia + Vue Router 4 + Axios
- **后端**：Spring Boot 3.5 + MyBatis-Plus + MySQL 8.0 + JWT
- **端口**：前端 5173，后端 8080
- **数据库**：`kg_tutoring_db`，用户 root，密码 983491037

## Git 工作流

```bash
# 有未推送改动时
git stash
git fetch origin
git reset --hard origin/dev   # 或 git pull --rebase origin dev
git stash pop

# 正常提交流程
git checkout dev
git pull origin dev
git add .
git commit -m "xxx"
git push origin dev
```

- Git 已配置：`hahedaftyudu-ly` / `hahedaftyudu@gmail.com`
- 队友：blossom-rt / shixy
- **不要**在 commit 里出现 Claude/Co-Authored-By

## 设计规范（AGENTS.md）

- 色彩：暖奶油底 `#faf7f2` + 暖橙强调 `#ff7b3d`，禁用紫色/靛蓝，不用 `ease-in-out`
- 纹理：全局 SVG feTurbulence 噪点层（App.vue，opacity 0.025）
- 全局样式：`src/styles/global.css` 覆盖 Element Plus CSS 变量
- 详细规范见：`AGENTS.md`

## 关键组件

| 组件 | 位置 | 用途 |
|---|---|---|
| `Sidebar.vue` | `src/components/` | 学生/教师端左侧导航，可折叠（useSidebar.js 共享状态），管理员不用 |
| `StudentHeader.vue` | `src/components/` | 统一页面头部（返回箭头+标题+操作插槽），返回按钮按角色跳首页 |
| `StudyPet.vue` | `src/components/` | 右下角学习伙伴宠物，答对庆祝/答错安慰/连击火焰 |
| `AnimatedCharacters.vue` | `src/components/` | 登录页四色角色插画，鼠标追踪+呼吸动画 |
| `usePet.js` | `src/composables/` | 宠物全局通信 |
| `useSidebar.js` | `src/composables/` | 侧边栏折叠状态共享 |

## 后端关键修复

- `StudentController.java` → `@RequestMapping("/api/student")`（已加 /api 前缀）
- `TeacherController.java` → `@RequestMapping("/api/teacher")`（已加 /api 前缀）
- `StudentWrongController.java` → 新增 `POST /wrong-questions` 错题收录接口
- Dashboard 进度计算：基于 `study_record.masteryLevel`（非 `path_detail.isFinished`）
- Vite 代理：`/api/kg` → `/api/*`，统一加 `/api` 前缀

## 启动命令

```bash
# 前端
cd kg-tutoring-frontend && npm run dev

# 后端（需先配阿里云Maven镜像 ~/.m2/settings.xml）
cd kg-tutoring-backend && mvn spring-boot:run -q
```

## 测试账号

- 学生：student / 123456
- 教师：teacher / 123456
- 管理员：admin / admin123

## Maven 镜像

已配置 `~/.m2/settings.xml` 阿里云镜像加速

## 测试数据

`db/test_data.sql` — 4课程/24知识点/36题/3路径，用完执行 `db/init_all.sql` 重建空库

## 本次会话主要改动

1. 全站暖调浅色主题重设计（37个页面）
2. 认证页不对称双栏布局 + 角色插画
3. 三端首页时间感知问候 + 图标管理入口
4. 学生/教师端侧边栏导航（可折叠）
5. 学习伙伴 StudyPet 桌面宠物
6. 错题本双选项卡（当前错题/曾经错题）+ 答对自动归档
7. 刷题答案归一化（ABCD→0123）+ 错题自动收录
8. 标记完成/进度条修复（masteryLevel vs isFinished）
9. 20+ 页面批量加 StudentHeader 返回导航
10. ER图（Chen记法）+ UML类图（5层架构）
11. UI UX Pro Max 设计系统集成

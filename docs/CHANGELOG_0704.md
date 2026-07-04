# 2026-07-04 前端改动记录

## 一、设计系统

### AGENTS.md — 前端设计规范
- 色彩系统：暖奶油底 `#faf7f2` + 暖橙强调 `#ff7b3d`，禁用紫色/靛蓝
- 纹理：全局 SVG feTurbulence 噪点颗粒层（App.vue，opacity 0.025）
- 动画：禁用 `ease-in-out`，交互用 `ease` 150-300ms
- 响应式：768px / 480px 断点 + `prefers-reduced-motion`
- 可访问性：`:focus-visible` 2px 橙色焦点环 + `cursor:pointer` 全局覆盖

### global.css — 全局样式
- 覆盖 Element Plus 全部 CSS 变量（背景/文字/边框/按钮/表格/对话框/菜单）
- 全局暗色滚动条、链接样式、markdown 内容样式
- 位置：`src/styles/global.css`

## 二、新增组件

### StudentHeader — 统一页面头部
- 返回箭头按钮 + 页面标题 + 副标题 + 右侧操作插槽 `#actions`
- 应用范围：学生端/教师端/管理员端所有子页面（20+ 页面）
- 位置：`src/components/StudentHeader.vue`

### StudyPet — 学习伙伴桌面宠物
- CSS 动画宠物，右下角陪伴学习（仅学生端可见）
- 答对：跳跃 + ✨🌟💫 粒子 + 星星眼 + 气泡鼓励
- 答错：变蓝安慰 + 💪📚 粒子 + "再试一次！"
- 连对 5 题：火焰模式 + 🔥⚡ 粒子
- 空闲：浮动 + 眨眼 + 随机喊话 + 可戳互动
- 通信：`usePet()` 组合式函数，子组件可触发宠物反应
- 位置：`src/components/StudyPet.vue` + `src/composables/usePet.js`

## 三、认证页重设计

### Login.vue
- 左右等宽双栏居中布局
- 左侧：品牌文字 + 四色角色插画（AnimatedCharacters）
- 右侧：表单卡片 + 角色切换 + 密码显隐手动 toggle
- 表单字段加 `label` 和 `autocomplete` 属性
- 角色三色主题：学生橙 / 教师绿 / 管理员金

### Register.vue / ForgotPassword.vue
- 同款不对称双栏布局
- 表单卡片暖白底 + 暖橙提交按钮

## 四、角色插画

### AnimatedCharacters
- 四色：焦糖棕 `#c1784e` / 深灰蓝 `#3d4a5c` / 活力橙 `#ff7b3d` / 暖金黄 `#f5a623`
- 交互：身体随鼠标轻微摆动（translateX/Y + skewX），瞳孔追踪鼠标
- 密码可见时黄色角色张嘴变 O 形
- 待机时不同节奏呼吸（scaleY 1.0-1.025）
- `transform-origin: bottom center` 底部始终对齐

## 五、首页重设计

### StudentHome
- 时间感知问候组件（日月小组件，白天橙底日球/夜晚灰底月牙）
- 问候语按小时自动切换（早上好/上午好/中午好/下午好/晚上好）
- 随机激励语副标题
- 学情统计四色渐变圆卡片
- 快捷入口：Element Plus 图标（Grid/Connection/MapLocation/Notebook）
- 快捷续学"继续"按钮补 @click 跳转

### TeacherHome
- 7 个管理入口网格（课程/知识点/图谱/题库/测评/主题/学情）
- 每入口含图标 + 标题 + 描述 + 独立配色
- 统计卡片四色渐变图标
- 时间感知问候

### AdminHome
- 6 个管理入口网格（用户/角色/课程/公告/日志/AI日志）
- 运营数据统计 + 操作日志卡片

## 六、问题修复

### 标记完成不更新
- 前端发 `{ masteryLevel: 2 }` 替代错误字段 `{ status: 'completed' }`
- NodeStudy 从学习记录读取真实掌握状态
- 涉及：`NodeStudy.vue` + `StudentRecordController.java`

### 进度条不显示
- Dashboard 进度计算从 `path_detail.isFinished` 改为 `study_record.masteryLevel`
- 涉及：`StudentController.java`

### 答题答案比对错误
- 数据库存字母答案（A/B/C/D）但选项 key 是数字索引（0/1/2/3）
- 归一化函数：A→0, B→1, C→2, D→3
- 涉及：`Practice.vue`

### 错题无法收录
- 后端新增 `POST /api/student/wrong-questions` 接口
- 前端 Practice 答错自动调用 `addWrongQuestion()`
- 反馈区显示正确答案 + 解析（同时读 `analysis` 和 `explanation` 字段）
- 涉及：`StudentWrongController.java` + `Practice.vue` + `student.js`

### Vite 代理路径
- 修复 `StudentController` 和 `TeacherController` 缺少 `/api` 前缀导致 404
- 统一所有 Controller 使用 `/api/` 前缀

## 七、类图与 ER 图

### ER 图（Chen 记法 · 黑白）
- 19 张表，矩形=实体 / 菱形=关系 / 椭圆=属性
- 强关联（FK 约束）实线 + 弱关联（日志）虚线
- 文件：`docs/diagrams/er_chen.dot` / `.png`

### 类图（UML · 黑白）
- 75 个类，5 层架构：Controller → Service接口 → ServiceImpl → Mapper → POJO
- △ 泛化 / → 关联 / ⇢ 依赖
- 文件：`docs/diagrams/class_diagram.dot` / `.png`

## 八、UI UX Pro Max 集成
- 安装 `uipro-cli` 全局命令行工具
- 技能安装到 `.claude/skills/ui-ux-pro-max/`
- 设计系统生成到 `docs/design-system/`
- 交付前检查清单：无 emoji 图标 / cursor:pointer / focus-visible / prefers-reduced-motion

## 九、测试数据脚本
- 文件：`db/test_data.sql`（已推送，用完可删）
- 4 课程 / 24 知识点 / 36 题目 / 3 学习路径 / 4 公告 / 3 跨学科主题 / 9 学习记录

## 附：项目文件结构

```
kg-intelligent-tutoring-system/
├── AGENTS.md                        # 前端设计规范
├── CHANGELOG_0704.md                # 本文档
├── db/
│   ├── init_all.sql                 # 数据库建表脚本
│   ├── seed_all.sql                 # 基础种子数据
│   └── test_data.sql                # 测试虚拟数据
├── docs/
│   ├── diagrams.md                  # UML 图文档
│   └── diagrams/
│       ├── er_chen.dot / .png       # ER 图 Chen 记法
│       ├── er_diagram.puml          # ER 图 IE 记法
│       ├── class_diagram.dot / .png # 类图
│       └── ...                      # 其他 UML 图
└── kg-tutoring-frontend/
    ├── src/
    │   ├── styles/global.css        # 全局设计系统
    │   ├── components/
    │   │   ├── AnimatedCharacters.vue  # 登录页角色插画
    │   │   ├── StudentHeader.vue       # 统一页面头部
    │   │   └── StudyPet.vue            # 学习伙伴宠物
    │   ├── composables/usePet.js       # 宠物通信
    │   └── views/                      # 全部页面（37 个 .vue）
    └── .claude/skills/ui-ux-pro-max/   # UI UX Pro Max
```

# 智能导学系统 — 前端设计规范

## 审美方向

《反主流》网页美学。拒绝 SaaS 模版感，追求触感和灵魂。现代但不冰冷，极简但有温度。
参考可汗学院 (zh.khanacademy.org) — 温馨、干净、学术但不压迫。

## 核心约束

- 不用紫色或靛蓝色渐变
- 不用纯平背景色（必须有颗粒纹理或渐变层次）
- 不用 Hero + 三卡片布局
- 不用完美居中对齐
- 不用 emoji 作为功能图标（全部使用 Element Plus Icons 或 SVG）
- 不用 `ease-in-out` 动画曲线（用 `ease`、`ease-out`、`cubic-bezier` 替代）
- 不堆砌高深专业名词和空话

## 色彩系统

| 用途 | 色值 | CSS 变量 | 说明 |
|---|---|---|---|
| 页面底色 | `#faf7f2` | `--bg-root` | 暖奶油色，有温度不刺眼 |
| 表面/卡片 | `#fffdf9` | `--bg-surface` | 暖白，比底色略亮 |
| 输入框填充 | `#f8f5f0` | `--bg-input` | 比表面稍暗，形成层次 |
| 悬停色 | `#f3efe8` | `--bg-hover` | 微暖的悬浮反馈 |
| 主强调色 | `#ff7b3d` | `--accent` | 暖橙，学生端 + 全局功能色 |
| 教师强调 | `#3d8a5e` | `--accent-green` | 沉稳绿，教师端顶栏/按钮 |
| 管理员强调 | `#b89030` | `--accent-gold` | 暖金，管理员端点缀 |
| 正文 | `#2d2a26` | `--text-primary` | 暖黑，柔和不刺眼（对比度 11.5:1） |
| 次要文字 | `#6b655e` | `--text-secondary` | 暖灰 |
| 提示文字 | `#a09a92` | `--text-muted` | 浅暖灰 |
| 边框 | `#e8e3db` | `--border-subtle` | 暖灰边框 |

### 角色主题色

| 角色 | 顶栏背景 | 强调色 |
|---|---|---|
| 学生 | `linear-gradient(135deg, #f5f1ea, #f0ece4)` | `#ff7b3d` |
| 教师 | `linear-gradient(135deg, #2d8a4e, #43b878)` | `#3d8a5e` |
| 管理员 | `linear-gradient(135deg, #f5f1ea, #f0ece4)` | `#b89030` |

## 布局原则

- 认证页（Login/Register/ForgotPassword）：不对称双栏，左侧 55% 品牌 + 插画，右侧 45% 表单卡片
- 功能页：全宽布局，顶部 `StudentHeader` 或角色顶栏，内容区 `padding: 24px 36px`
- 卡片圆角：12-16px，`1px solid #e8e3db` 边框 + 微妙阴影 `0 2px 12px rgba(0,0,0,0.04)`
- 管理入口网格：4-6 列，每个入口含图标 + 标题 + 描述，hover 上浮 3px
- 统计卡片行：4 列 grid，图标 + 数值 + 标签

## 通用组件

### StudentHeader (`src/components/StudentHeader.vue`)

所有学生端/教师端/管理员端子页面统一使用。
- 返回箭头按钮（`ArrowLeft` 图标，调用 `router.back()`）
- 页面标题 + 副标题
- 右侧操作插槽 `#actions`

### 时间感知问候

首页顶栏根据当前小时自动切换：
- 6:00-9:00：早上好 | 9:00-12:00：上午好 | 12:00-14:00：中午好 | 14:00-18:00：下午好 | 18:00-6:00：晚上好
- 随机激励语作为副标题
- 左侧小时钟/日月小组件（白天橙底日球，夜晚灰底月牙）

### 管理入口卡片

```html
<div class="entry-grid">
  <div class="entry-item" @click="...">
    <div class="entry-icon" :style="{ background: bg, color: color }">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <span class="entry-label">标题</span>
    <span class="entry-desc">描述</span>
  </div>
</div>
```

### 快捷入口卡片（学生端）

4 个 Element Plus 图标（`Grid` / `Connection` / `MapLocation` / `Notebook`），2×2 网格，白底卡片 + 微阴影。

## 动画规范

- 交互反馈：`ease`，150-300ms
- 入场动画：`cubic-bezier(0.22, 0.61, 0.36, 1)`（弹性收尾）
- 角色呼吸：`ease`，不同角色 3.0s-4.0s 不同周期
- 禁用：`ease-in-out`、`linear`（除加载 spinner）
- `prefers-reduced-motion` 时关闭全部动画（`animation-duration: 0.01ms`）

## 纹理

- 全页面（`App.vue`）叠加 SVG `feTurbulence` 噪点层，opacity 0.025
- 颗粒纹理 `pointer-events: none; z-index: 9999` 覆盖所有页面但不阻挡交互

## 字体

- 系统字体栈：`"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", system-ui, sans-serif`
- 标题：700-800 weight，字间距 +2px
- 正文：400 weight，行高 1.5-1.75
- 英文标签：字间距 +3px

## 角色插画（AnimatedCharacters）

- 四色：焦糖棕 `#c1784e` / 深灰蓝 `#3d4a5c` / 活力橙 `#ff7b3d` / 暖金黄 `#f5a623`
- 交互：身体随鼠标轻微摆动（translateX/Y + skewX），瞳孔追踪鼠标方向
- 密码可见时黄色角色张嘴变 O
- 待机时不同节奏微微呼吸（scaleY 1.0-1.025）
- `transform-origin: bottom center`，底部始终对齐

## 可访问性（UI UX Pro Max 检查清单）

### 必须通过

- [x] 无 emoji 作为图标 — 全站 Element Plus Icons
- [x] `cursor: pointer` — 全局 `.clickable`、`.entry-item`、`.quick-item` 等
- [x] `:focus-visible` — 全局 2px 橙色焦点环，`outline-offset: 2px`
- [x] `prefers-reduced-motion` — 全局关闭动画
- [x] `overflow-x: hidden` — 防止移动端横向滚动
- [x] 文字对比度 ≥ 4.5:1 — 暖黑 `#2d2a26` 对奶油 `#faf7f2` = 11.5:1
- [x] 触摸目标 ≥ 44×44px — 按钮、图标、入口卡片均满足
- [x] 响应式断点：768px（平板）、480px（手机）
- [x] 表单标签正确关联（Element Plus `el-form-item` 自动处理）
- [x] 加载状态 — 按钮 `:loading`、表格 `v-loading`、骨架屏
- [x] 错误反馈 — `ElMessage.error` 在 API 拦截器中统一处理

### 全局 CSS 规则位置

`src/styles/global.css` — 覆盖 Element Plus CSS 变量 + 基础样式 + 可访问性

### 设计系统文件

`docs/design-system/kg智能导学系统/MASTER.md` — UI UX Pro Max 自动生成

## 前端技术栈

- Vue 3 + Vite
- Element Plus 2.x（组件库）
- ECharts 5.x（知识图谱可视化）
- Pinia（状态管理）
- Vue Router 4.x
- Axios（HTTP 请求，`src/utils/request.js` 统一拦截）

## 项目文件结构约定

```
src/
├── views/
│   ├── Login.vue / Register.vue / ForgotPassword.vue  # 认证页（不对称分栏）
│   ├── StudentHome.vue / TeacherHome.vue / AdminHome.vue  # 三端首页
│   ├── student/   # 学生端子页面（GraphView, PathList, Practice, WrongBook...）
│   ├── teacher/   # 教师端子页面（CRUD 管理页）
│   └── admin/     # 管理员端子页面（CRUD 管理页）
├── components/
│   ├── AnimatedCharacters.vue   # 登录页四色角色插画
│   └── StudentHeader.vue        # 统一页面头部（带返回箭头）
├── styles/
│   └── global.css               # 全局设计系统 + Element Plus 覆盖
├── api/                         # API 函数（student.js, teacher.js, admin.js, knowledge.js, user.js）
├── utils/
│   └── request.js               # Axios 实例 + 拦截器
└── router/
    └── index.js                 # 路由表 + 导航守卫
```

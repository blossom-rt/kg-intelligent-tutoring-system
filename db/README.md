# 数据库脚本使用说明

本目录用于初始化 `kg_tutoring_db` 数据库，并导入项目演示数据。

## 目录结构

```
db/
├── init_full.sql             ← 一键初始化（全新环境就用它）
├── docker-init.sql           ← Docker 兼容入口（当前为空操作）
├── data_sources.md           ← 演示数据来源说明
├── README.md                 ← 本文件
├── init/                     ← 分步初始化脚本
│   ├── init_all.sql              ← 第 1 步：建库建表 + 角色 + admin
│   ├── seed_all.sql              ← 第 2 步：导入全部演示数据
│   ├── init8.sql                 ← 第 3 步：建 chapter 表 + 回填 chapter_id
│   ├── init9.sql                 ← 第 4 步：删除旧 chapter 文本字段
│   └── enrich_learning_materials.sql ← 第 5 步：补充知识点学习资料
└── migrate/                  ← 维护/迁移脚本
    ├── cleanup.sql               ← 清空业务数据（保留主账号）
    ├── add_knowledge_node_model_fields.sql ← 旧库升级：补 node_type 等字段
    ├── add_learning_resource.sql ← 旧库升级：补 learning_resource 表
    ├── init8.sql ← 旧库升级：建 chapter 表 + 迁移章节数据
    ├── init9.sql ← 旧库升级：删除旧 chapter 列
    ├── seed_learning_resource_demo.sql ← 为知识点生成演示视频资源
    ├── add_practice_question_types.sql ← 旧库升级：为每个知识点补多选题/判断题
    ├── top_up_practice_questions_to_7.sql ← 旧库升级：把每个知识点题量补到约 7 道
    └── migrate_exam.sql          ← 旧库升级：补 exam 表 + 演示测评
```

## 快速开始

### 方式 A：一键初始化（推荐）

```bash
mysql -u root -p < db/init_full.sql
```

一条命令完成：建库 → 建表 → 导入全部演示数据 → 迁移章节表 → 补充学习资料 → 补齐练习题。

### 方式 B：分步执行

```bash
mysql -u root -p < db/init/init_all.sql
mysql -u root -p < db/init/seed_all.sql
mysql -u root -p < db/init/init8.sql
mysql -u root -p < db/init/init9.sql
mysql -u root -p < db/init/enrich_learning_materials.sql
mysql -u root -p < db/migrate/top_up_practice_questions_to_7.sql
```

效果与 `init_full.sql` 完全相同。

### 方式 C：Docker

使用项目根目录的 `docker-compose.yml`，它会按数字前缀自动执行初始化脚本：建表 → 导入演示数据 → 迁移章节表 → 补学习资料 → 补多选/判断题 → 将每个知识点题量补到约 7 道。`docker-init.sql` 仅保留为兼容入口，不再负责串联脚本。

## 数据重置

推荐使用完整重建，避免旧结构和新结构混用：

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS kg_tutoring_db;"
mysql -u root -p < db/init_full.sql
```

`cleanup.sql` 只用于清空业务数据调试，不建议再接 `seed_all.sql` 作为完整重置流程；当前后端使用 `chapter_id`，而 `seed_all.sql` 是导入演示数据的中间步骤，需要在 `init8.sql`、`init9.sql` 迁移前执行。

## 旧库升级顺序

旧库升级建议按下面顺序执行。脚本基本是幂等的，已存在的字段、表或题目不会重复无限叠加。

如果旧库还没有 exam、exam_question 表（7 月 2 日前建的库）：

```bash
mysql -u root -p < db/migrate/migrate_exam.sql
```

如果旧库还没有 `node_type`、`learning_goal` 等知识点扩展字段：

```bash
mysql -u root -p < db/migrate/add_knowledge_node_model_fields.sql
```

如果旧库还没有 `chapter` 表，或 `knowledge_node` 仍是旧的 `chapter` 文本字段：

```bash
mysql -u root -p < db/migrate/init8.sql
mysql -u root -p < db/migrate/init9.sql
```

如果旧库还没有 learning_resource 表：

```bash
mysql -u root -p < db/migrate/add_learning_resource.sql
```

如果需要为已有知识点快速生成演示视频资源：

```bash
mysql -u root -p < db/migrate/seed_learning_resource_demo.sql
```

如果旧库需要补充学生端知识点练习题型：

```bash
mysql -u root -p < db/migrate/add_practice_question_types.sql
mysql -u root -p < db/migrate/top_up_practice_questions_to_7.sql
```

上述脚本会先为每个知识点追加 1 道多选题和 1 道判断题，再按当前题量动态补齐到约 7 道；题型字段使用 `single`、`multi`、`judge`，多选答案使用英文逗号分隔，例如 `A,B,C`。

最后补学习资料：

```bash
mysql -u root -p < db/init/enrich_learning_materials.sql
```

## 测试账号

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `admin123` | 管理员 |
| `teacher` | `123456` | 教师 |
| `student` | `123456` | 学生 |
| `teacher_math` | `123456` | 教师 |
| `teacher_science` | `123456` | 教师 |
| `teacher_it` | `123456` | 教师 |
| `student01` - `student12` | `123456` | 学生 |

## 导入后主要数据量

执行 `init_full.sql`（或按分步脚本完整执行）后，数据库共有 **22 张表**，主要数据量约：

| 表 | 数量 | 说明 |
|---|---:|---|
| `sys_role` | 3 | admin / teacher / student |
| `sys_user` | 18 | 3 主账号 + 3 教师 + 12 学生 |
| `sys_email_code` | 0 | 邮箱验证码（运行时产生） |
| `course` | 9 | 含 1 门跨学科课程 |
| `chapter` | 随知识点章节自动生成 | 由旧 `knowledge_node.chapter` 迁移而来 |
| `knowledge_node` | 56 | |
| `learning_resource` | 0 | 教师配置知识点视频/资料后产生 |
| `knowledge_edge` | 55 | 含跨学科 support 边 |
| `question` | 约 7 × 知识点数 | 初始化题 + 多选/判断题 + 自动补齐题 |
| `study_record` | 370 | |
| `study_path` | — | |
| `path_detail` | — | |
| `exam` | 3 | |
| `exam_question` | 13 | |
| `exam_record` | 40 | |
| `wrong_question` | 66 | |
| `user_favorite` | 0 | 用户收藏（运行时产生） |
| `cross_subject_theme` | 3 | |
| `cross_theme_node` | 12 | |
| `sys_notice` | 2 | |
| `sys_oper_log` | 2 | |
| `ai_call_log` | 0 | AI 调用时产生 |

验证命令：

```bash
mysql -u root -p kg_tutoring_db -e "
SELECT 'sys_user' 表名, COUNT(*) 数量 FROM sys_user
UNION ALL SELECT 'course', COUNT(*) FROM course
UNION ALL SELECT 'chapter', COUNT(*) FROM chapter
UNION ALL SELECT 'knowledge_node', COUNT(*) FROM knowledge_node
UNION ALL SELECT 'learning_resource', COUNT(*) FROM learning_resource
UNION ALL SELECT 'question', COUNT(*) FROM question
UNION ALL SELECT 'study_record', COUNT(*) FROM study_record
UNION ALL SELECT 'exam', COUNT(*) FROM exam
UNION ALL SELECT 'exam_question', COUNT(*) FROM exam_question
UNION ALL SELECT 'exam_record', COUNT(*) FROM exam_record
UNION ALL SELECT 'wrong_question', COUNT(*) FROM wrong_question;
"
```

题型分布验证：

```bash
mysql -u root -p kg_tutoring_db -e "
SELECT question_type 题型, COUNT(*) 数量
FROM question
GROUP BY question_type
ORDER BY question_type;
"
```

每个知识点题量验证：

```bash
mysql -u root -p kg_tutoring_db -e "
SELECT MIN(cnt) 最少题数, MAX(cnt) 最多题数, ROUND(AVG(cnt), 2) 平均题数
FROM (
  SELECT kn.id, COUNT(q.id) cnt
  FROM knowledge_node kn
  LEFT JOIN question q ON q.node_id = kn.id
  GROUP BY kn.id
) t;
"
```

## 数据来源说明

详细来源见 [`data_sources.md`](data_sources.md)。

- 课程和知识点参考公开课程标准主题整理。
- 题目是围绕知识点自编的演示题。
- 学生、教师、学习记录、测评记录、错题均为虚构测试数据。

## 注意事项

- MySQL 用户名若不是 `root`，请替换命令中的 `root`。
- 后端连接配置在 `kg-tutoring-backend/src/main/resources/application.properties`。
- `seed_all.sql` 是初始化链中的演示数据导入步骤，应在章节迁移前执行；已迁移到 `chapter_id` 后不要单独重跑它。
- `top_up_practice_questions_to_7.sql` 按当前题量补齐到 7 道，重复执行不会继续增加。
- 完全重置：先 `DROP DATABASE kg_tutoring_db`，再重跑 `init_full.sql`。

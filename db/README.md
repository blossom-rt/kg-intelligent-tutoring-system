# 数据库脚本使用说明

本目录用于初始化 `kg_tutoring_db` 数据库，并导入项目演示数据。

## 推荐使用的两个脚本

组员一般只需要使用这两个脚本：

| 脚本 | 作用 |
|---|---|
| `init_all.sql` | 创建数据库、创建所有表、初始化基础角色和管理员账号 |
| `seed_all.sql` | 导入课程、知识点、题库、学生、教师、学习记录、测评记录、错题和主题等演示数据 |
| `enrich_learning_materials.sql` | 为每个知识点补充结构化学习资料，可重复执行 |
| `migrate_exam.sql` | 旧库补测评定义表和演示测评数据，已全新初始化时不需要执行 |

## 全新数据库使用方式

如果本地还没有初始化过数据库，执行：

```bash
cd kg-intelligent-tutoring-system/db

mysql -u root -p < init_all.sql
mysql -u root -p < seed_all.sql
mysql -u root -p < enrich_learning_materials.sql
```

执行完成后会生成数据库：

```text
kg_tutoring_db
```

## 已经建过表的情况

如果数据库和表已经存在，只想补充或刷新演示数据，执行：

```bash
cd kg-intelligent-tutoring-system/db

mysql -u root -p < seed_all.sql
mysql -u root -p < enrich_learning_materials.sql
```

`seed_all.sql` 会导入完整演示数据，并完成必要的数据清理。`enrich_learning_materials.sql` 会把知识点的一句话简介扩展为“学习目标、核心概念、学习步骤、例题导学、常见误区、练习建议、自测清单”等结构化学习资料。

如果旧库还没有教师发布测评功能所需的 `exam`、`exam_question` 表，先执行：

```bash
cd kg-intelligent-tutoring-system/db

mysql -u root -p < migrate_exam.sql
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

正常导入后大致会有：

| 表 | 数量 |
|---|---:|
| `sys_role` | 3 |
| `sys_user` | 18 |
| `course` | 9 |
| `knowledge_node` | 56 |
| `knowledge_edge` | 55 |
| `question` | 56 |
| `study_record` | 370 |
| `exam` | 3 |
| `exam_question` | 13 |
| `exam_record` | 40 |
| `wrong_question` | 66 |
| `cross_subject_theme` | 3 |
| `cross_theme_node` | 12 |

可以用下面命令检查：

```bash
mysql -u root -p kg_tutoring_db -e "
SELECT 'sys_user' 表名, COUNT(*) 数量 FROM sys_user
UNION ALL SELECT 'course', COUNT(*) FROM course
UNION ALL SELECT 'knowledge_node', COUNT(*) FROM knowledge_node
UNION ALL SELECT 'question', COUNT(*) FROM question
UNION ALL SELECT 'study_record', COUNT(*) FROM study_record
UNION ALL SELECT 'exam', COUNT(*) FROM exam
UNION ALL SELECT 'exam_question', COUNT(*) FROM exam_question
UNION ALL SELECT 'exam_record', COUNT(*) FROM exam_record
UNION ALL SELECT 'wrong_question', COUNT(*) FROM wrong_question;
"
```

## 数据来源说明

详细来源见：

```text
data_sources.md
```

简要说明：

- 课程和知识点参考公开课程标准主题整理。
- 题目是围绕知识点自编的演示题。
- 学生、教师、学习记录、测评记录、错题均为虚构测试数据。
- 不直接复制网上题库原题。

## 注意事项

- 如果 MySQL 用户不是 `root`，把命令里的 `root` 换成自己的用户名。
- 如果后端连接失败，检查 `kg-tutoring-backend/src/main/resources/application.properties` 里的数据库账号密码。
- `seed_all.sql` 可以重复执行，核心演示数据不会无限重复。
- 如果想完全重置数据库，可以先删除库再重新执行 `init_all.sql` 和 `seed_all.sql`。

重置命令示例：

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS kg_tutoring_db;"

cd kg-intelligent-tutoring-system/db
mysql -u root -p < init_all.sql
mysql -u root -p < seed_all.sql
```

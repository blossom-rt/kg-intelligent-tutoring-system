# 前后端接口文档

记录日期：2026-07-03

本文档根据当前前端 `src/api` 调用和后端 `controller` 代码整理，用于组员联调、答辩说明和后续维护。

## 1. 通用约定

### 1.1 服务地址

开发环境：

```text
前端：http://localhost:5173
后端：http://127.0.0.1:8080
```

前端 Axios 配置：

```text
baseURL = /api/kg
```

Vite 代理规则：

```text
/api/kg/*  ->  http://127.0.0.1:8080/api/*
```

因此前端代码中：

```js
request.get('/auth/info')
```

实际请求后端：

```text
GET /api/auth/info
```

### 1.2 认证方式

除登录、注册、发送验证码、重置密码外，其他 `/api/**` 接口都需要请求头携带 Token：

```http
Authorization: Bearer <token>
```

公开接口：

```text
POST /api/auth/login
POST /api/auth/register
POST /api/auth/send-code
POST /api/auth/reset-password
```

### 1.3 统一返回格式

后端大多数接口返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

前端响应拦截器会自动解包 `data`，所以前端业务代码拿到的是 `data` 字段本身。

常见错误：

```json
{
  "code": 401,
  "message": "未登录",
  "data": null
}
```

```json
{
  "code": 500,
  "message": "无权限",
  "data": null
}
```

## 2. 认证接口

前端文件：`kg-tutoring-frontend/src/api/user.js`

后端控制器：`AuthController`

### 2.1 登录

```text
POST /api/auth/login
```

前端调用：

```js
login(data)
```

请求体：

```json
{
  "username": "student",
  "password": "123456",
  "role": "student"
}
```

说明：

- `role` 可传 `admin`、`teacher`、`student`。
- 后端会校验账号、密码、角色和账号状态。

返回数据：

```json
{
  "token": "jwt-token",
  "role": "student"
}
```

### 2.2 获取当前登录信息

```text
GET /api/auth/info
```

前端调用：

```js
getUserInfo()
```

返回数据：

```json
{
  "token": "new-token",
  "role": "student"
}
```

### 2.3 注册

```text
POST /api/auth/register
```

前端调用：

```js
register(data)
```

请求体：

```json
{
  "username": "new_student",
  "password": "123456",
  "realName": "张三",
  "email": "new_student@test.com",
  "code": "123456"
}
```

说明：

- 注册默认角色为学生。
- 验证码需要先调用发送验证码接口。

### 2.4 发送邮箱验证码

```text
POST /api/auth/send-code
```

前端调用：

```js
sendEmailCode(data)
```

请求体：

```json
{
  "email": "new_student@test.com",
  "type": "register"
}
```

说明：

- `type` 可用值：`register`、`reset`。
- 当前实现会将验证码打印到后端控制台。

### 2.5 重置密码

```text
POST /api/auth/reset-password
```

前端调用：

```js
resetPassword(data)
```

请求体：

```json
{
  "email": "student@test.com",
  "code": "123456",
  "password": "newPassword"
}
```

## 3. 个人信息接口

前端文件：`kg-tutoring-frontend/src/api/user.js`

后端控制器：`UserController`

### 3.1 修改密码

```text
PUT /api/user/update-password
```

前端调用：

```js
updatePassword(data)
```

请求体：

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

### 3.2 获取个人信息

```text
GET /api/user/profile
```

前端调用：

```js
getProfile()
```

当前返回：

```json
{
  "token": "new-token",
  "role": "student"
}
```

说明：当前后端返回的是登录视图对象，不是完整 `SysUser` 信息。

### 3.3 修改个人信息

```text
PUT /api/user/profile
```

前端调用：

```js
updateProfile(data)
```

请求体：

```json
{
  "realName": "张三",
  "email": "zhangsan@test.com"
}
```

## 4. 管理员接口

前端文件：`kg-tutoring-frontend/src/api/admin.js`

后端控制器：`AdminController`

权限要求：`admin`

### 4.1 角色管理

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 查询角色 | GET | `/api/admin/roles` | `getRoleList()` | 已实现 |
| 新增角色 | POST | `/api/admin/roles` | `createRole(data)` | 已实现 |
| 修改角色 | PUT | `/api/admin/roles/{id}` | `updateRole(id, data)` | 已实现 |
| 删除角色 | DELETE | `/api/admin/roles/{id}` | `deleteRole(id)` | 已实现 |

角色请求体示例：

```json
{
  "roleCode": "teacher",
  "roleName": "教师",
  "description": "负责课程和题库管理"
}
```

### 4.2 用户管理

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 查询用户 | GET | `/api/admin/users` | `getUserList(params)` | 已实现 |
| 新增用户 | POST | `/api/admin/users` | `createUser(data)` | 已实现 |
| 修改用户 | PUT | `/api/admin/users/{id}` | `updateUser(id, data)` | 已实现 |
| 删除用户 | DELETE | `/api/admin/users/{id}` | `deleteUser(id)` | 已实现 |
| 启用/禁用 | PUT | `/api/admin/users/{id}/status` | `toggleUserStatus(id, data)` | 已实现 |

用户请求体示例：

```json
{
  "username": "teacher_new",
  "password": "123456",
  "realName": "新教师",
  "email": "teacher_new@test.com",
  "roleId": 2,
  "status": 1
}
```

启用/禁用请求体：

```json
{
  "status": 1
}
```

### 4.3 日志查询

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 操作日志 | GET | `/api/admin/oper-logs` | `getOperLogs(params)` | 已实现 |
| 调用日志 | GET | `/api/admin/ai-logs` | `getAiLogs(params)` | 已实现，当前演示数据为空 |

操作日志查询参数：

```text
module 可选
```

示例：

```text
GET /api/admin/oper-logs?module=数据库
```

## 5. 课程、知识点、知识边接口

前端文件：`kg-tutoring-frontend/src/api/knowledge.js`

后端控制器：`CourseController`、`NodeController`、`EdgeController`

### 5.1 课程管理

| 功能 | 方法 | 路径 | 前端函数 | 权限 | 状态 |
|---|---|---|---|---|---|
| 查询课程 | GET | `/api/courses` | `getCourseList(params)` | 登录用户 | 已实现 |
| 课程详情 | GET | `/api/courses/{id}` | `getCourseById(id)` | 登录用户 | 已实现 |
| 新增课程 | POST | `/api/courses` | `createCourse(data)` | admin | 已实现 |
| 修改课程 | PUT | `/api/courses/{id}` | `updateCourse(id, data)` | admin | 已实现 |
| 删除课程 | DELETE | `/api/courses/{id}` | `deleteCourse(id)` | admin | 已实现 |

课程请求体示例：

```json
{
  "subject": "数学",
  "courseName": "数与代数综合",
  "description": "课程简介",
  "teacherId": 2
}
```

### 5.2 知识点管理

| 功能 | 方法 | 路径 | 前端函数 | 权限 | 状态 |
|---|---|---|---|---|---|
| 查询知识点 | GET | `/api/nodes` | `getNodeList(params)` | 登录用户 | 已实现 |
| 知识点详情 | GET | `/api/nodes/{id}` | `getNodeById(id)` | 登录用户 | 已实现 |
| 新增知识点 | POST | `/api/nodes` | `createNode(data)` | teacher | 已实现 |
| 修改知识点 | PUT | `/api/nodes/{id}` | `updateNode(id, data)` | teacher | 已实现 |
| 删除知识点 | DELETE | `/api/nodes/{id}` | `deleteNode(id)` | teacher | 已实现 |

查询参数：

```text
courseId 可选
```

知识点请求体示例：

```json
{
  "courseId": 1,
  "name": "一元一次方程",
  "description": "理解方程思想",
  "difficulty": 2,
  "chapter": "第二章 方程",
  "expectedMinutes": 40
}
```

### 5.3 知识边管理

| 功能 | 方法 | 路径 | 前端函数 | 权限 | 状态 |
|---|---|---|---|---|---|
| 查询知识边 | GET | `/api/edges` | `getEdgeList(params)` | 登录用户 | 已实现 |
| 新增知识边 | POST | `/api/edges` | `createEdge(data)` | teacher | 已实现 |
| 删除知识边 | DELETE | `/api/edges/{id}` | `deleteEdge(id)` | teacher | 已实现 |

查询参数：

```text
nodeId 可选，表示 fromNodeId
```

知识边请求体示例：

```json
{
  "fromNodeId": 1,
  "toNodeId": 2,
  "relationType": "prerequisite",
  "isCrossSubject": 0
}
```

## 6. 题库接口

前端文件：`kg-tutoring-frontend/src/api/teacher.js`

后端控制器：`QuestionController`

| 功能 | 方法 | 路径 | 前端函数 | 权限 | 状态 |
|---|---|---|---|---|---|
| 查询题目 | GET | `/api/questions` | `getQuestionList(params)` | 登录用户 | 已实现 |
| 新增题目 | POST | `/api/questions` | `createQuestion(data)` | teacher | 已实现 |
| 修改题目 | PUT | `/api/questions/{id}` | `updateQuestion(id, data)` | teacher | 已实现 |
| 删除题目 | DELETE | `/api/questions/{id}` | `deleteQuestion(id)` | teacher | 已实现 |

查询参数：

```text
nodeId 可选
```

题目请求体示例：

```json
{
  "nodeId": 1,
  "content": "方程 2x + 3 = 11 的解是？",
  "options": "{\"A\":\"x=2\",\"B\":\"x=3\",\"C\":\"x=4\",\"D\":\"x=7\"}",
  "answer": "C",
  "analysis": "两边先减 3，再除以 2。",
  "difficulty": 2,
  "questionType": "single"
}
```

## 7. 学生端接口

前端文件：`kg-tutoring-frontend/src/api/student.js`

### 7.1 知识图谱

后端控制器：`StudentGraphController`

```text
GET /api/student/graph
```

前端调用：

```js
getStudentGraph(params)
```

查询参数：

```text
courseId 可选
```

返回数据：

```json
{
  "nodes": [],
  "edges": []
}
```

状态：已实现。

### 7.2 学习路径

后端控制器：`StudentPathController`

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 按目标知识点生成路径 | POST | `/api/student/path/generate` | `generatePath(data)` | 已实现 |
| 按跨学科主题生成路径 | POST | `/api/student/path/generate-by-theme` | `generatePathByTheme(data)` | 已实现 |
| 查询路径列表 | GET | `/api/student/path/list` | `getPathList()` | 已实现 |
| 查询路径详情 | GET | `/api/student/path/{id}` | `getPathDetail(id)` | 已实现，当前只返回路径主表 |
| 更新节点完成状态 | PUT | `/api/student/path/detail/{id}` | `updatePathDetail(id, data)` | 已实现 |
| 删除路径 | DELETE | `/api/student/path/{id}` | `deletePath(id)` | 已实现 |

生成路径请求体：

```json
{
  "targetNodeId": 3
}
```

按主题生成请求体：

```json
{
  "themeId": 1
}
```

### 7.3 学习记录

后端控制器：`StudentRecordController`

```text
PUT /api/student/study-record
```

前端调用：

```js
updateStudyRecord(data)
```

请求体：

```json
{
  "nodeId": 1,
  "masteryLevel": 2,
  "correctRate": 85.5,
  "studyMinutes": 30
}
```

状态：已实现。

### 7.4 练习

后端控制器：`StudentPracticeController`

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 获取练习题 | GET | `/api/student/practice/questions` | `getPracticeQuestions(params)` | 已实现 |
| 提交练习 | POST | `/api/student/practice/submit` | `submitPractice(data)` | 已实现，错题记录未接入 |

获取练习题查询参数：

```text
nodeId 必填
```

提交练习请求体：

```json
{
  "nodeId": 1,
  "answers": [
    {
      "questionId": 1,
      "answer": "A"
    }
  ]
}
```

提交练习返回数据：

```json
{
  "total": 1,
  "correctCount": 1,
  "correctRate": 100.0,
  "masteryLevel": 3,
  "message": "表现优秀！"
}
```

### 7.5 测评

后端控制器：`StudentExamController`

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 我的测评列表 | GET | `/api/student/exams` | `getStudentExams()` | 已实现 |
| 测评详情 | GET | `/api/student/exam/{id}` | `getExamResult(id)` | 已实现 |
| 提交测评 | POST | `/api/student/exam/submit` | `submitExam(data)` | 已实现 |

提交测评请求体：

```json
{
  "courseId": 1,
  "answers": [
    {
      "questionId": 1,
      "answer": "A"
    },
    {
      "questionId": 2,
      "answer": "B"
    }
  ]
}
```

说明：

- 提交后会生成 `exam_record`。
- 答错题会写入 `wrong_question`。

### 7.6 错题本

后端控制器：`StudentWrongController`

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 查询错题 | GET | `/api/student/wrong-questions` | `getWrongQuestions(params)` | 已实现 |
| 删除错题 | DELETE | `/api/student/wrong-questions/{id}` | `deleteWrongQuestion(id)` | 已实现 |

说明：前端函数支持 `params`，当前后端未使用筛选参数。

### 7.7 个人分析

后端控制器：`StudentAnalysisController`

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 个人学习统计 | GET | `/api/student/analysis/personal` | `getPersonalAnalysis()` | 已实现 |
| 薄弱知识点 | GET | `/api/student/analysis/weak` | `getWeakAnalysis()` | 已实现，部分统计简化 |

个人学习统计返回示例：

```json
{
  "totalStudiedNodes": 6,
  "masteredNodes": 2,
  "averageCorrectRate": 68.33,
  "totalStudyMinutes": 158,
  "totalExams": 3,
  "wrongQuestionCount": 5
}
```

### 7.8 学生端扩展能力占位接口

后端控制器：`StudentAiController`

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 知识点总结 | POST | `/api/student/ai/node-summary` | `aiNodeSummary(data)` | 占位 |
| 错题解析 | POST | `/api/student/ai/wrong-explain` | `aiWrongExplain(data)` | 占位 |
| 测评报告 | POST | `/api/student/ai/exam-report` | `aiExamReport(data)` | 占位 |

请求体示例：

```json
{
  "nodeId": 1
}
```

当前返回：

```json
"AI 功能开发中"
```

## 8. 教师端接口

前端文件：`kg-tutoring-frontend/src/api/teacher.js`

### 8.1 教师测评管理

后端控制器：`TeacherExamController`

| 功能 | 方法 | 路径 | 前端函数 | 状态 |
|---|---|---|---|---|
| 查询测评 | GET | `/api/teacher/exams` | `getExamList(params)` | 占位，返回空列表 |
| 创建测评 | POST | `/api/teacher/exams` | `createExam(data)` | 占位 |
| 修改测评 | PUT | `/api/teacher/exams/{id}` | `updateExam(id, data)` | 占位 |
| 删除测评 | DELETE | `/api/teacher/exams/{id}` | `deleteExam(id)` | 占位 |

查询参数：

```text
courseId 必填
```

创建测评请求体示例：

```json
{
  "courseId": 1,
  "questionIds": [1, 2, 3],
  "totalScore": 100
}
```

说明：当前后端尚未真正实现教师创建考试。

### 8.2 班级学情分析

后端控制器：`TeacherAnalysisController`

```text
GET /api/teacher/analysis/class
```

前端调用：

```js
getClassAnalysis(params)
```

查询参数：

```text
courseId 必填
```

状态：已实现基础返回，但分析逻辑简化。

当前返回示例：

```json
{
  "totalNodes": 8,
  "averageMastery": 0,
  "studentCount": 0,
  "message": "班级分析功能待完善"
}
```

## 9. 跨学科主题接口

前端文件：`kg-tutoring-frontend/src/api/admin.js`

后端控制器：`ThemeController`

| 功能 | 方法 | 路径 | 前端函数 | 权限 | 状态 |
|---|---|---|---|---|---|
| 查询主题 | GET | `/api/themes` | `getThemeList(params)` | 登录用户 | 已实现 |
| 主题详情 | GET | `/api/themes/{id}` | `getThemeDetail(id)` | 登录用户 | 已实现 |
| 新增主题 | POST | `/api/themes` | `createTheme(data)` | teacher | 已实现 |
| 修改主题 | PUT | `/api/themes/{id}` | `updateTheme(id, data)` | teacher | 已实现 |
| 删除主题 | DELETE | `/api/themes/{id}` | `deleteTheme(id)` | teacher | 已实现 |
| 切换状态 | PUT | `/api/themes/{id}/status` | `toggleThemeStatus(id, data)` | teacher | 已实现 |

主题请求体示例：

```json
{
  "themeName": "校园运动会数据分析",
  "description": "结合数学、物理和信息科技分析运动成绩。",
  "difficulty": 2,
  "publisherId": 2,
  "status": 1
}
```

说明：`toggleThemeStatus` 后端不读取请求体，只根据 `id` 切换状态。

## 10. 公告接口

前端文件：`kg-tutoring-frontend/src/api/admin.js`

后端控制器：`NoticeController`

| 功能 | 方法 | 路径 | 前端函数 | 权限 | 状态 |
|---|---|---|---|---|---|
| 查询公告 | GET | `/api/notices` | `getNoticeList(params)` | 登录用户 | 已实现 |
| 新增公告 | POST | `/api/notices` | `createNotice(data)` | admin | 已实现 |
| 修改公告 | PUT | `/api/notices/{id}` | `updateNotice(id, data)` | admin | 已实现 |
| 删除公告 | DELETE | `/api/notices/{id}` | `deleteNotice(id)` | admin | 已实现 |

公告请求体示例：

```json
{
  "title": "系统通知",
  "content": "本周完成阶段测评。",
  "publisherId": 5,
  "targetRole": "all"
}
```

## 11. 旧接口说明

项目中还存在两个早期 Controller：

```text
/student/**
/teacher/**
```

这些接口直接返回 `ResponseEntity`，不完全遵循统一 `Result` 格式。当前前端主要使用 `/api/**` 接口，后续建议统一迁移到 `/api/**`，避免维护两套路径。

旧学生接口包括：

```text
GET /student/dashboard
GET /student/courses
GET /student/knowledge-nodes
GET /student/study-paths
GET /student/study-records
GET /student/wrong-questions
GET /student/exam-records
GET /student/cross-subjects
```

旧教师接口包括：

```text
GET /teacher/dashboard
GET /teacher/courses
POST /teacher/courses
PUT /teacher/courses/{id}
DELETE /teacher/courses/{id}
GET /teacher/knowledge-nodes
POST /teacher/knowledge-nodes
PUT /teacher/knowledge-nodes/{id}
DELETE /teacher/knowledge-nodes/{id}
GET /teacher/questions
```

## 12. 当前接口完成度总结

| 模块 | 完成度 |
|---|---|
| 登录注册 | 基本完成 |
| 用户资料 | 基本完成，资料返回字段可再完善 |
| 管理员角色/用户/公告/日志 | 基本完成 |
| 课程/知识点/知识边 | 基本完成 |
| 题库管理 | 基本完成 |
| 学生知识图谱 | 基本完成 |
| 学习路径 | 基本完成，但详情接口只返回主表 |
| 练习 | 可用，错题记录未接入练习提交 |
| 测评 | 学生提交可用，教师测评管理未完成 |
| 错题本 | 基本完成 |
| 学情分析 | 有基础数据，班级分析较简化 |
| 跨学科主题 | 基本完成 |
| 扩展能力占位接口 | 仅占位 |


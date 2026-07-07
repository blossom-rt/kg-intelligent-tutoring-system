# KG 智能导学系统 — UML 图（标准记法）

> 所有图均采用 **PlantUML** 绘制，遵循 UML 2.x 标准记法。
> 源文件位于 `docs/diagrams/` 目录，可用 VS Code + PlantUML 插件直接编辑预览，
> 或运行 `plantuml -tpng *.puml` 批量重新渲染。

---

## 1. 用例图 (Use Case Diagram)

**标准 UML 三要素**：系统边界（矩形） + 参与者/Actor（火柴人） + 用例（椭圆）

![用例图](diagrams/kg_usecase.png)

<details>
<summary>📄 PlantUML 源文件 (01_usecase.puml)</summary>

```plantuml
@startuml kg_usecase
left to right direction
actor "学生" as Student
actor "教师" as Teacher
actor "管理员" as Admin
actor "邮箱服务" as Email <<外部系统>>

rectangle "KG 智能导学系统" as System {
    usecase "登录" as UC_Login
    usecase "注册" as UC_Register
    usecase "浏览知识图谱" as UC_BrowseKG
    usecase "生成学习路径" as UC_GenPath
    usecase "刷题练习" as UC_Practice
    usecase "参加测评" as UC_Exam
    usecase "查看错题本" as UC_WrongBook
    usecase "查看学情报告" as UC_Report
    usecase "知识点管理" as UC_NodeMgr
    usecase "题库管理" as UC_QuestionMgr
    usecase "跨学科主题发布" as UC_CrossTheme
    usecase "学情分析" as UC_Analytics
    usecase "用户管理" as UC_UserMgr
    usecase "操作日志审计" as UC_Audit
    usecase "公告管理" as UC_Notice
    usecase "发送验证码" as UC_SendCode
    usecase "拓扑排序计算" as UC_TopoSort
    usecase "AI 诊断分析" as UC_AIDiag

    UC_Register ..> UC_SendCode : <<extend>>
    UC_GenPath ..> UC_TopoSort : <<include>>
    UC_Exam ..> UC_AIDiag : <<include>>
}

Student --> UC_Login
Student --> UC_Register
Student --> UC_BrowseKG
Student --> UC_GenPath
Student --> UC_Practice
Student --> UC_Exam
Student --> UC_WrongBook
Student --> UC_Report
Teacher --> UC_Login
Teacher --> UC_NodeMgr
Teacher --> UC_QuestionMgr
Teacher --> UC_CrossTheme
Teacher --> UC_Analytics
Admin --> UC_Login
Admin --> UC_UserMgr
Admin --> UC_Audit
Admin --> UC_Notice
Email --> UC_SendCode
@enduml
```
</details>

---

## 2. 顺序图 — 登录 (Sequence Diagram)

**标准 UML**：带生命线的 Actor/Participant、激活框、alt/else 组合片段

![顺序图-登录](diagrams/kg_seq_login.png)

<details>
<summary>📄 PlantUML 源文件 (02_seq_login.puml)</summary>

```plantuml
@startuml kg_seq_login
actor "用户" as U
participant "Vue 前端" as F
participant "SpringBoot\n后端" as B
database "MySQL" as D

U -> F : 选择角色，输入账号密码
activate F
F -> B : POST /user/login\n{username, password, role}
activate B
B -> D : SELECT * FROM sys_user\nWHERE username = ?
activate D
D --> B : 用户记录
deactivate D
B -> B : MD5(密码)比对 + role_id 校验

alt 校验通过
    B --> F : 200 OK {token, role, userInfo}
    deactivate B
    F -> F : 存入 localStorage，路由跳转
    F --> U : 跳转角色首页
else 校验失败
    B --> F : 401 Unauthorized
    deactivate B
    F --> U : 提示错误信息
end
deactivate F
@enduml
```
</details>

---

## 3. 顺序图 — 注册 (Sequence Diagram)

**标准 UML**：分段 (`== ==`)、Note 注释、生命线

![顺序图-注册](diagrams/kg_seq_register.png)

<details>
<summary>📄 PlantUML 源文件 (03_seq_register.puml)</summary>

```plantuml
@startuml kg_seq_register
actor "学生" as S
participant "Vue 前端" as F
participant "SpringBoot\n后端" as B
database "MySQL" as D

== 获取验证码 ==
S -> F : 填写注册信息，点击获取验证码
activate F
F -> B : GET /user/send-code?email=xxx&type=register
activate B
B -> B : 生成 6 位随机验证码
B -> D : INSERT INTO sys_email_code
activate D
D --> B : OK
deactivate D
note right of B : 开发阶段 console 输出验证码
B --> F : {message: "验证码已发送"}
deactivate B
F --> S : 提示"验证码已发送至邮箱"

== 提交注册 ==
S -> F : 输入验证码，点击注册
F -> B : POST /user/register
activate B
B -> D : 校验账号唯一性 + 验证码有效性
activate D
D --> B : 校验结果
deactivate D

alt 校验通过
    B -> B : MD5 加密密码，role_id = 3
    B -> D : INSERT INTO sys_user
    activate D
    D --> B : OK
    deactivate D
    B --> F : 200 OK 注册成功
    deactivate B
    F --> S : 跳转登录页
else 校验失败
    B --> F : 400 Bad Request
    deactivate B
    F --> S : 显示错误提示
end
deactivate F
@enduml
```
</details>

---

## 4. 顺序图 — 学习流程 (Sequence Diagram)

**标准 UML**：分段、loop 循环片段、内嵌 alt 片段、自调用

![顺序图-学习](diagrams/kg_seq_study.png)

<details>
<summary>📄 PlantUML 源文件 (04_seq_study.puml)</summary>

```plantuml
@startuml kg_seq_study
actor "学生" as S
participant "Vue 前端" as F
participant "SpringBoot\n后端" as B
database "MySQL" as D

== 进入知识图谱 ==
S -> F : 进入知识图谱页面
activate F
F -> B : GET /student/knowledge-nodes
activate B
B -> D : SELECT * FROM knowledge_node
D --> B : 知识点列表
B --> F : 知识点列表 JSON
deactivate B
F -> F : 渲染知识点卡片
F --> S : 展示知识点图谱

== 生成学习路径 ==
S -> F : 选择目标知识点
F -> B : POST /student/study-path/generate
activate B
B -> B : 拓扑排序算法\n从目标节点反向 BFS
B -> D : INSERT INTO study_path
D --> B : path_id
loop 每个路径节点
    B -> D : INSERT INTO path_detail
    D --> B : OK
end
B --> F : {pathId, nodes, totalMinutes}
deactivate B
F --> S : 展示学习路径节点列表

== 按序学习 + 练习 ==
loop 逐个节点
    S -> F : 学习知识点 + 完成练习
    F -> B : POST /student/submit-answer
    activate B
    B -> D : INSERT INTO study_record
    D --> B : OK
    B -> B : 计算正确率
    alt 正确率 >= 80%
        B -> D : UPDATE mastery_level = 2
        B --> F : 达标，解锁下一节点
    else 正确率 < 80%
        B -> D : UPDATE mastery_level = 1
        B --> F : 未达标，建议继续刷题
    end
    deactivate B
    F --> S : 显示判分结果
end
deactivate F
@enduml
```
</details>

---

## 5. 通信图/协作图 (Communication Diagram)

**标准 UML**：对象矩形（`:ClassName`） + 链路（link） + 编号消息序列（`1:`, `2:`, `2.1:` 等嵌套编号）

![通信图](diagrams/05_communication.png)

> 此图使用 Graphviz DOT 绘制（PlantUML 不原生支持通信图）。
> 渲染命令：`dot -Tpng 05_communication.dot -o 05_communication.png`

<details>
<summary>📄 DOT 源文件 (05_communication.dot)</summary>

```dot
digraph KG_Communication {
    rankdir=LR;
    splines=polyline;

    subgraph cluster_frontend {
        label="<<component>> Vue 前端";
        login       [label="登录页\n:LoginView", shape=rect, style="rounded,filled"];
        register    [label="注册页\n:RegisterView", shape=rect, style="rounded,filled"];
        stuhome     [label="学生首页\n:StudentHome", shape=rect, style="rounded,filled"];
        kggraph     [label="知识图谱\n:KGGraph", shape=rect, style="rounded,filled"];
        wrongbook   [label="错题本\n:WrongBook", shape=rect, style="rounded,filled"];
        teachwp     [label="教师工作台\n:TeachWorkplace", shape=rect, style="rounded,filled"];
        adminp      [label="管理后台\n:AdminPanel", shape=rect, style="rounded,filled"];
    }

    subgraph cluster_backend {
        label="<<component>> SpringBoot 后端";
        auth  [label="认证接口\n:AuthController", shape=rect, style="rounded,filled"];
        stu   [label="学生接口\n:StudentController", shape=rect, style="rounded,filled"];
        teach [label="教师接口\n:TeacherController", shape=rect, style="rounded,filled"];
    }

    subgraph cluster_database {
        label="<<component>> MySQL 数据库";
        user  [label="sys_user", shape=cylinder, style=filled];
        role  [label="sys_role", shape=cylinder, style=filled];
        knode [label="knowledge_node", shape=cylinder, style=filled];
        path  [label="study_path", shape=cylinder, style=filled];
        wrong [label="wrong_question", shape=cylinder, style=filled];
    }

    /* 编号消息 */
    login     -> auth  [label="1: POST /user/login"];
    auth      -> user  [label="1.1: SELECT"];
    auth      -> role  [label="1.2: 校验 role_id"];
    register  -> auth  [label="2: send-code\n3: register"];
    stuhome   -> stu   [label="4: GET study-path"];
    kggraph   -> stu   [label="5: GET knowledge-nodes"];
    stu       -> path  [label="4.1: INSERT/SELECT"];
    stu       -> knode [label="5.1: SELECT"];
    wrongbook -> stu   [label="6: GET wrong-questions"];
    stu       -> wrong [label="6.1: SELECT"];
    teachwp   -> teach [label="7: CRUD /teacher/*"];
    teach     -> knode [label="7.1: INSERT/UPDATE"];
    teach     -> user  [label="7.2: SELECT 学情"];
    adminp    -> user  [label="8: 用户管理 CRUD"];
}
```
</details>

---

## 6. 活动图 — 学生完整学习流程 (Activity Diagram)

**标准 UML**：起始/终止节点、活动（圆角矩形）、菱形判断、while 循环

![活动图-学生学习](diagrams/kg_activity_student.png)

<details>
<summary>📄 PlantUML 源文件 (06_activity_student.puml)</summary>

```plantuml
@startuml kg_activity_student
start
:访问系统;
if (已注册?) then (否)
    :填写注册信息;
    :获取邮箱验证码;
    :提交注册;
endif
:登录系统;
if (首次登录?) then (是)
    :进入新手引导;
else (否)
    :进入学情看板;
    if (有进行中的路径?) then (是)
        :快捷续学;
    endif
endif
:浏览知识图谱;
:选择目标知识点;
:生成学习路径;
while (还有未完成节点?) is (是)
    :学习知识点内容;
    :完成配套练习;
    :提交答案;
    if (正确率 ≥ 80%?) then (是)
        :标记已掌握\nmastery_level = 2;
    else (否)
        :标记学习中\nmastery_level = 1;
        :继续刷题;
    endif
endwhile (否)
:参加章节测评;
if (测评通过?) then (是)
    :生成 AI 诊断报告;
    stop
else (否)
    :错题自动收录;
    :进入错题本;
    :查看错题讲解;
    :复习错题;
    :再次测评;
    detach
endif
@enduml
```
</details>

---

## 7. 活动图 — 教师工作流程 (Activity Diagram)

**标准 UML**：泳道分区、活动、判断

![活动图-教师](diagrams/kg_activity_teacher.png)

<details>
<summary>📄 PlantUML 源文件 (07_activity_teacher.puml)</summary>

```plantuml
@startuml kg_activity_teacher
|教师|
start
:登录系统;
:进入工作台看板;
if (操作类型?) then (课程管理)
    :创建/编辑课程;
    :管理知识点;
    :配置难度与依赖;
    :管理题库;
    if (跨学科主题?) then (是)
        :创建跨学科主题;
        :关联多学科知识点;
        :发布到学生端;
    endif
else (学情分析)
    :选择班级/课程;
    :查看整体掌握度;
    :查看学生个体详情;
    :导出学情报表;
endif
:更新教学内容;
stop
@enduml
```
</details>

---

## 8. 活动图 — 注册与验证码流程 (Activity Diagram)

**标准 UML**：泳道分区、多级判断、超时处理

![活动图-注册](diagrams/kg_activity_register.png)

<details>
<summary>📄 PlantUML 源文件 (08_activity_register.puml)</summary>

```plantuml
@startuml kg_activity_register
|学生|
start
:进入注册页面;
:填写信息\n用户名、邮箱、密码;
:输入邮箱;

|系统|
if (60 秒内已发送?) then (是)
    |学生|
    :等待倒计时;
else (否)
    |系统|
    :生成 6 位随机验证码;
    :存入 sys_email_code;
    :输出验证码;
endif

|学生|
:输入验证码;
:提交注册表单;

|系统|
if (验证码正确?) then (否)
    :提示"验证码错误";
    stop
endif
if (5 分钟内?) then (否)
    :提示"验证码已过期";
    stop
endif
if (账号唯一?) then (否)
    :提示"账号已存在";
    stop
endif

:MD5 加密密码;
:INSERT INTO sys_user;
:标记验证码已使用;

|学生|
:提示"注册成功";
:跳转登录页;
stop
@enduml
```
</details>

---

## 记法对照说明

| 旧版问题 | 新版改进 |
|---|---|
| 用例图用 `graph LR` 拼凑，无系统边界、无椭圆用例 | 标准 UML：矩形边界 + Actor + 椭圆用例 + `<<extend>>`/`<<include>>` |
| 协作图用 flowchart 代替，只有箭头无消息编号 | 标准 UML 通信图：对象矩形 + 链路 + `1:`, `2:`, `1.1:` 嵌套消息编号 |
| 流程图用 `flowchart LR`，无起始/终止节点 | 标准 UML 活动图：实心起止点 + 圆角活动 + 菱形判断 + 泳道 |
| 顺序图缺少激活框和分段 | 补充 `activate/deactivate`、`== 分段 ==`、`note` 注释 |

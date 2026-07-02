# KG 智能导学系统 — UML 图

## 1. 用例图

```mermaid
graph LR
    Student((学生)) --> A1[登录/注册]
    Student --> A2[浏览知识图谱]
    Student --> A3[生成学习路径]
    Student --> A4[刷题练习]
    Student --> A5[参加测评]
    Student --> A6[查看错题本]
    Student --> A7[学情报告]

    Teacher((教师)) --> B1[登录]
    Teacher --> B2[知识点管理]
    Teacher --> B3[题库管理]
    Teacher --> B4[跨学科主题发布]
    Teacher --> B5[学情分析]

    Admin((管理员)) --> C1[登录]
    Admin --> C2[用户管理]
    Admin --> C3[操作日志审计]
    Admin --> C4[公告管理]
```

## 2. 顺序图 — 登录

```mermaid
sequenceDiagram
    actor U as 用户
    participant F as Vue前端
    participant B as SpringBoot
    participant D as MySQL

    U->>F: 选择角色/输入账号密码
    F->>B: POST /user/login
    B->>D: 查询sys_user
    D-->>B: 用户记录
    B->>B: MD5比对+角色校验
    alt 校验通过
        B-->>F: {token, role}
        F->>F: 存localStorage
        F->>U: 跳转角色首页
    else 校验失败
        B-->>F: 401
        F->>U: 提示错误
    end
```

## 3. 顺序图 — 注册

```mermaid
sequenceDiagram
    actor S as 学生
    participant F as Vue前端
    participant B as SpringBoot
    participant D as MySQL

    S->>F: 填表,获取验证码
    F->>B: GET /user/send-code
    B->>B: 生成6位随机码
    B->>D: INSERT sys_email_code
    B-->>F: 验证码已发送
    Note over B: console输出验证码
    S->>F: 输入验证码,提交
    F->>B: POST /user/register
    B->>D: 校验账号唯一性+验证码
    B->>B: MD5加密密码(role_id=3)
    B->>D: INSERT sys_user
    B-->>F: 注册成功
    F->>S: 跳转登录页
```

## 4. 顺序图 — 学习流程

```mermaid
sequenceDiagram
    actor S as 学生
    participant F as Vue前端
    participant B as SpringBoot
    participant D as MySQL

    S->>F: 进入知识图谱
    F->>B: GET /student/knowledge-nodes
    B->>D: SELECT knowledge_node
    D-->>B: 知识点列表
    B-->>F: 返回数据
    F->>S: 展示知识点卡片
    S->>F: 选目标,生成学习路径
    F->>B: 生成路径(拓扑排序)
    B->>D: INSERT study_path
    B-->>F: 路径节点列表
    S->>F: 按顺序学习+做题
    F->>B: 提交练习答案
    B->>D: INSERT study_record
    alt 正确率达标
        B->>D: UPDATE mastery_level=2
    else 未达标
        B->>D: UPDATE mastery_level=1
    end
    B-->>F: 判分结果
    F->>S: 解锁下一节点/继续刷题
```

## 5. 协作图（通信图）

```mermaid
graph LR
    subgraph 前端Vue
        Login[登录页]
        Register[注册页]
        StuHome[学生首页]
        KGGraph[知识图谱]
        WrongB[错题本]
        TeachWP[教师工作台]
        AdminP[管理后台]
    end
    subgraph 后端SpringBoot
        Auth[认证接口]
        Stu[学生接口]
        Teach[教师接口]
    end
    subgraph MySQL
        User[(sys_user)]
        Role[(sys_role)]
        Node[(knowledge_node)]
        Path[(study_path)]
        Wrong[(wrong_question)]
    end
    Login --> Auth --> User
    Login --> Auth --> Role
    Register --> Auth --> User
    StuHome --> Stu --> Path
    KGGraph --> Stu --> Node
    WrongB --> Stu --> Wrong
    TeachWP --> Teach --> Node
    TeachWP --> Teach --> User
    AdminP --> User
```

## 6. 流程图 — 学生完整学习流程

```mermaid
flowchart LR
    A[访问系统] --> B{已注册?}
    B -->|否| C[注册]
    C --> D[登录]
    B -->|是| D
    D --> E{首次登录?}
    E -->|是| F[新手引导]
    E -->|否| G[学情看板]
    F --> H[浏览知识图谱]
    G --> H
    G --> I[快捷续学]
    H --> J[选目标知识点]
    I --> K[继续学习节点]
    J --> K
    K --> L[学内容+做练习]
    L --> M{达标?}
    M -->|是| N[标记完成]
    M -->|否| O[刷题巩固]
    O --> L
    N --> P{全部完成?}
    P -->|否| K
    P -->|是| Q[章节测评]
    Q --> R{通过?}
    R -->|是| S[AI诊断报告]
    R -->|否| T[错题本]
    T --> U[复习错题]
    U --> Q
```

## 7. 流程图 — 教师工作流程

```mermaid
flowchart LR
    A[教师登录] --> B[工作台看板]
    B --> C[管理课程]
    C --> D[管理知识点]
    D --> E[配置难度/依赖]
    E --> F[管理题库]
    F --> G[关联知识点]
    G --> H[发布跨学科主题]
    H --> I[发布到学生端]
    B --> J[学情分析]
    J --> K[查看掌握度]
    J --> L[查看个体详情]
    J --> M[导出报表]
```

## 8. 流程图 — 注册与验证码流程

```mermaid
flowchart LR
    A[学生填表] --> B[输入邮箱]
    B --> C{60秒内发过?}
    C -->|是| D[等待倒计时]
    D --> B
    C -->|否| E[生成6位验证码]
    E --> F[存入sys_email_code]
    F --> G[console打印验证码]
    G --> H[学生输入验证码]
    H --> I{验证码正确?}
    I -->|否| J[提示错误]
    J --> H
    I -->|是| K{5分钟内?}
    K -->|否| L[验证码过期]
    L --> B
    K -->|是| M[MD5加密密码]
    M --> N[写入sys_user]
    N --> O[标记验证码已用]
    O --> P[注册成功]
```


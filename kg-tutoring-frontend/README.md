# 知识图谱智能导学系统前端

本目录是项目的 Vue 3 + Vite 前端工程。

## Docker 启动方式

如果使用项目根目录的 Docker Compose 一键启动：

```sh
cd ..
docker compose up -d --build
```

前端访问地址：

```text
http://localhost
```

Docker 方式下，前端会先执行 `npm run build`，再由 Nginx 容器托管打包后的静态文件。Nginx 使用默认 `80` 端口，所以访问地址是 `http://localhost`，不是 `http://localhost:5173`。

前端容器中已经配置接口代理：

```text
/api/kg/* -> backend:8080/api/*
```

## 本地开发方式

如果需要前端热更新开发，可以单独启动 Vite 开发服务器：

```sh
npm install
npm run dev
```

本地开发访问地址：

```text
http://localhost:5173
```

`localhost:5173` 只对应本地 Vite 开发服务器；Docker 启动时不使用这个端口。

## 构建

```sh
npm run build
```

## 代码检查

```sh
npm run lint
```

## 常用测试账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 学生 | `student` | `123456` |
| 教师 | `teacher` | `123456` |
| 管理员 | `admin` | `admin123` |

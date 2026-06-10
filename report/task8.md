# 机场航班运行仿真系统 Task 8 开发记录

记录时间：2026-05-29
项目路径：`D:\Desktop\airport-flight-simulation`
任务名称：新增登录界面

## 1. 任务目标

原系统访问 `/` 后直接进入首页仪表盘，入口表现较单调。本次新增一个机场场景风格的登录界面，让用户先通过登录页进入系统，再跳转到运行概览首页。

## 2. 完成内容

1. 将根路径 `/` 调整为登录页入口。
2. 新增 `POST /login`，提交账号密码后跳转到 `/dashboard`。
3. 将原首页仪表盘迁移到 `/dashboard`。
4. 新增 `login.html` 登录模板。
5. 在 `app.css` 中新增登录页机场场景样式。
6. 将已有页面导航栏中的“首页”链接从 `/` 改为 `/dashboard`。
7. 更新 `WebControllerTest`，覆盖登录页、登录跳转和首页仪表盘访问。

## 3. 页面效果说明

登录页采用方案 B 的机场场景风格：

- 左侧展示系统名称和“进入航班仿真中心”的入口文案。
- 背景使用浅蓝到浅黄色的机场清晨感色彩。
- 右侧放置登录表单，默认填入演示账号。
- 页面中包含跑道线条和飞机标识，增强机场主题感。

演示账号：

```text
账号：admin
密码：123456
```

说明：本次只做课程项目演示入口，没有引入 Spring Security 或真实权限控制，避免影响现有页面和业务流程。

## 4. 修改文件

```text
src/main/java/com/example/airportsimulation/controller/DashboardController.java
src/main/resources/templates/login.html
src/main/resources/templates/*.html
src/main/resources/static/css/app.css
src/test/java/com/example/airportsimulation/controller/WebControllerTest.java
```

## 5. 验证结果

控制器测试：

```powershell
mvn test '-Dtest=WebControllerTest'
```

结果：

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

完整测试：

```powershell
mvn test
```

结果：

```text
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

本机页面验证使用 8081 端口启动新实例：

```text
GET / -> 200，标题为“系统登录 - 机场航班运行仿真系统”
POST /login -> 302，跳转到 /dashboard
GET /dashboard -> 200，标题为“机场航班运行仿真系统”，页面包含“运行概览”
```

## 6. 当前状态

Task 8 已完成。访问 `http://localhost:8081/` 可看到新增登录页，点击“进入系统”后进入原首页仪表盘。

# 机场航班运行仿真系统 Task 6 开发记录

记录时间：2026-05-29
项目路径：`D:\Desktop\airport-flight-simulation`
任务名称：实现 Controller 和 Thymeleaf 页面

## 1. 任务目标

Task 6 的目标是在已有 Service 层基础上补齐 Web 表现层，使系统可以通过浏览器完成课程实验演示。

本次完成：

1. 首页仪表盘。
2. 航班 CRUD 页面。
3. 跑道 CRUD 页面。
4. 停机位 CRUD 页面。
5. 仿真任务创建、单步推进、自动运行和重置页面。
6. 仿真日志页面。
7. 统计结果页面。
8. 统一页面样式。
9. Web Controller 测试。

## 2. 新增 Controller

```text
src/main/java/com/example/airportsimulation/controller/DashboardController.java
src/main/java/com/example/airportsimulation/controller/StatisticsController.java
src/main/java/com/example/airportsimulation/controller/FlightController.java
src/main/java/com/example/airportsimulation/controller/RunwayController.java
src/main/java/com/example/airportsimulation/controller/GateController.java
src/main/java/com/example/airportsimulation/controller/SimulationController.java
src/main/java/com/example/airportsimulation/controller/RecordController.java
```

Controller 只负责页面路由、表单绑定和 Model 数据组织，具体业务规则继续由已有 Service 层处理。

## 3. 页面路由

| 路由 | 功能 |
| --- | --- |
| `GET /` | 首页仪表盘 |
| `GET /statistics` | 统计结果 |
| `GET /flights` | 航班列表、关键字和状态筛选 |
| `GET /flights/new` | 新增航班表单 |
| `POST /flights` | 保存新增航班 |
| `GET /flights/{id}/edit` | 编辑航班表单 |
| `POST /flights/{id}` | 保存编辑航班 |
| `POST /flights/{id}/delete` | 删除航班 |
| `GET /runways` | 跑道列表 |
| `GET /runways/new` | 新增跑道表单 |
| `POST /runways` | 保存新增跑道 |
| `GET /runways/{id}/edit` | 编辑跑道表单 |
| `POST /runways/{id}` | 保存编辑跑道 |
| `POST /runways/{id}/delete` | 删除跑道 |
| `GET /gates` | 停机位列表 |
| `GET /gates/new` | 新增停机位表单 |
| `POST /gates` | 保存新增停机位 |
| `GET /gates/{id}/edit` | 编辑停机位表单 |
| `POST /gates/{id}` | 保存编辑停机位 |
| `POST /gates/{id}/delete` | 删除停机位 |
| `GET /simulation` | 仿真运行页面 |
| `POST /simulation/tasks` | 创建仿真任务 |
| `POST /simulation/tasks/{id}/step` | 单步推进仿真 |
| `POST /simulation/tasks/{id}/run` | 自动运行仿真 |
| `POST /simulation/reset` | 重置仿真 |
| `GET /records` | 仿真日志 |

## 4. 新增页面和样式

```text
src/main/resources/templates/dashboard.html
src/main/resources/templates/statistics.html
src/main/resources/templates/flights.html
src/main/resources/templates/flight-form.html
src/main/resources/templates/runways.html
src/main/resources/templates/runway-form.html
src/main/resources/templates/gates.html
src/main/resources/templates/gate-form.html
src/main/resources/templates/simulation.html
src/main/resources/templates/records.html
src/main/resources/static/css/app.css
```

页面风格采用简洁后台系统样式，主要由导航栏、统计卡片、表格、表单和操作按钮组成。没有引入额外前端框架，保持项目依赖简单。

## 5. 测试说明

新增测试类：

```text
src/test/java/com/example/airportsimulation/controller/WebControllerTest.java
```

覆盖内容：

1. 首页可以加载统计数据。
2. 统计页可以加载。
3. 航班列表和航班表单可以加载。
4. 新增航班提交后重定向到航班列表。
5. 跑道和停机位列表可以加载。
6. 没有仿真任务时，仿真页面仍可以正常加载。
7. 创建任务和单步推进操作会重定向回仿真页。
8. 日志页面可以加载。

## 6. 测试先行过程

先新增 `WebControllerTest` 并执行：

```powershell
mvn test '-Dtest=WebControllerTest'
```

首次执行结果为编译失败，原因是以下 Controller 尚未实现：

```text
DashboardController
StatisticsController
FlightController
RunwayController
GateController
SimulationController
RecordController
```

随后实现 Controller，再次执行时进入模板解析阶段，失败原因变为 Thymeleaf 模板不存在，符合测试先行预期。

补齐模板和 CSS 后，定向测试结果：

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 7. 完整验证结果

执行完整测试：

```powershell
mvn test
```

最终结果：

```text
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 8. 当前状态

Task 6 已完成。项目现在具备从浏览器访问的完整基础页面，可以进行航班、跑道、停机位管理，也可以创建并运行仿真任务、查看仿真日志和统计结果。

下一步可以继续 Task 7：README 和手工验证说明。

# 机场航班运行仿真系统 Task 7 开发记录

记录时间：2026-05-29
项目路径：`D:\Desktop\airport-flight-simulation`
任务名称：README 和手工验证说明

## 1. 任务目标

Task 7 的目标是补齐项目交付说明，并对 Web 页面做一次手工运行验证。

本次完成：

1. 新增根目录 `README.md`。
2. 说明项目简介、技术栈和环境要求。
3. 说明 MySQL 初始化方式。
4. 说明 `application.yml` 数据库账号配置。
5. 说明启动命令、页面入口和测试命令。
6. 启动 Spring Boot 项目并访问主要页面。
7. 记录手工验证流程和最终测试结果。

## 2. README 内容说明

新增文件：

```text
README.md
```

README 覆盖内容如下：

| 内容 | 说明 |
| --- | --- |
| 项目简介 | 说明系统用于航班、跑道、停机位管理和航班运行仿真 |
| 技术栈 | Java 17、Spring Boot、Spring MVC、Spring Data JPA、Thymeleaf、MySQL、H2、Maven、JUnit |
| 环境要求 | JDK 17、Maven 3.x、MySQL 8.x、浏览器 |
| MySQL 初始化 | 使用 `database/airport_simulation.sql` 初始化数据库和演示数据 |
| 数据库账号 | 说明 `src/main/resources/application.yml` 默认使用 `root/root`，本机不一致时需要修改 |
| 启动命令 | 提供 `mvn spring-boot:run` 和打包后 `java -jar` 两种方式 |
| 页面入口 | 列出首页、航班、跑道、停机位、仿真、日志、统计页面 |
| 测试命令 | 提供 `mvn test` 和 `mvn test '-Dtest=WebControllerTest'` |

## 3. MySQL 配置检查

当前运行配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/airport_simulation?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: <本机 MySQL 密码>
```

本机手工检查结果：

```powershell
mysql -uroot -p -e "SELECT VERSION() AS mysql_version;"
```

结果为：

```text
mysql_version
8.0.46
```

说明当前机器上的 MySQL 账号已经可以登录。随后执行初始化脚本：

```powershell
mysql --default-character-set=utf8mb4 -uroot -p < database/airport_simulation.sql
```

首次执行时发现 MySQL 8.0 对 `simulation_task.current_time` 建表字段解析失败，原因是 `current_time` 与 MySQL 时间函数关键字冲突。已将初始化脚本中的字段写法调整为：

```sql
`current_time` DATETIME
```

修正后初始化成功，数据库内容检查结果：

```text
Tables_in_airport_simulation
flight
gate
runway
simulation_record
simulation_task

flight_count: 8
runway_count: 2
gate_count: 3
```

## 4. 手工验证流程

本次页面手工验证使用当前 `application.yml` 的 MySQL 配置启动打包后的 Spring Boot jar。

启动方式：

```powershell
mvn package '-DskipTests'
java -jar target/airport-flight-simulation-0.0.1-SNAPSHOT.jar
```

启动日志确认：

```text
Tomcat started on port 8080 (http) with context path '/'
Started AirportSimulationApplication
```

随后访问：

```text
http://localhost:8080
```

## 5. 页面验证结果

| 页面 | 地址 | 验证结果 |
| --- | --- | --- |
| 首页 | `/` | HTTP 200，标题为“机场航班运行仿真系统” |
| 航班管理 | `/flights` | HTTP 200，标题为“航班管理” |
| 跑道管理 | `/runways` | HTTP 200，标题为“跑道管理” |
| 停机位管理 | `/gates` | HTTP 200，标题为“停机位管理” |
| 仿真运行 | `/simulation` | HTTP 200，标题为“仿真运行” |
| 仿真日志 | `/records` | HTTP 200，标题为“仿真日志” |
| 统计结果 | `/statistics` | HTTP 200，标题为“统计结果” |

页面访问命令输出摘要：

```text
GET / -> 200, title=机场航班运行仿真系统
GET /flights -> 200, title=航班管理
GET /runways -> 200, title=跑道管理
GET /gates -> 200, title=停机位管理
GET /simulation -> 200, title=仿真运行
GET /records -> 200, title=仿真日志
GET /statistics -> 200, title=统计结果
```

## 6. 最终测试结果

执行完整自动化测试：

```powershell
mvn test
```

最终结果：

```text
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

执行打包验证：

```powershell
mvn package '-DskipTests'
```

最终结果：

```text
BUILD SUCCESS
```

## 7. 当前状态

Task 7 已完成。项目根目录已经具备 README 交付说明，MySQL 初始化脚本已在本机执行成功，Web 页面已在 MySQL 配置下完成手工访问验证，自动化测试和打包验证均通过。

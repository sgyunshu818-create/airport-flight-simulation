# 机场航班运行仿真系统

本项目是一个基于 Spring Boot 的机场航班运行仿真系统，用于管理航班、跑道、停机位等基础数据，并通过仿真任务模拟航班在跑道和停机位资源上的运行过程。系统提供 Web 页面入口，可以先进入登录页，再查看首页仪表盘、航班管理、跑道管理、停机位管理、仿真运行、仿真日志和统计结果。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL 8.x
- H2 Database，本地便携运行和测试环境使用
- Maven 3.x
- JUnit 5 / Spring Boot Test

## 环境要求

- JDK 17 或以上
- Maven 3.8 或以上
- MySQL 8.x，正式 MySQL 模式需要
- 浏览器，推荐 Chrome / Edge

## MySQL 初始化

项目提供初始化脚本：

```text
database/airport_simulation.sql
```

在 MySQL 中执行：

```powershell
mysql -uroot -p < database/airport_simulation.sql
```

脚本会创建 `airport_simulation` 数据库，重建以下表并写入演示数据：

- `flight`
- `runway`
- `gate`
- `simulation_task`
- `simulation_record`

注意：脚本中包含 `DROP TABLE IF EXISTS`，执行前请确认当前数据库中的数据可以被重置。

## 数据库账号配置

运行配置位于：

```text
src/main/resources/application.yml
```

数据库连接配置示例：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/airport_simulation?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:<your-password>}
```

如果本机 MySQL 账号或密码不同，推荐用环境变量覆盖，不需要改源码：

```powershell
$env:DB_USERNAME='root'
$env:DB_PASSWORD='your-password'
$env:DB_URL='jdbc:mysql://localhost:3306/airport_simulation?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
mvn spring-boot:run
```

不要将真实数据库密码提交到公共仓库。

## 任意环境访问

项目现在支持两种启动方式：

### 云服务器 Docker 部署

如果希望所有人都能访问，推荐部署到云服务器。服务器需要开放一个公网 IP，并安装 Docker 和 Docker Compose。

1. 将项目上传到服务器。

2. 进入项目目录，创建 `.env`：

```bash
cp .env.example .env
```

3. 编辑 `.env`，至少修改数据库密码：

```text
APP_PORT=8080
MYSQL_ROOT_PASSWORD=change_this_root_password
MYSQL_USER=airport_user
MYSQL_PASSWORD=change_this_app_password
```

4. 启动服务：

```bash
docker compose up -d --build
```

5. 查看运行状态：

```bash
docker compose ps
docker compose logs -f app
```

6. 访问地址：

```text
http://服务器公网IP:8080/
```

如果使用云厂商安全组，需要放行 `APP_PORT` 对应端口，例如 `8080/TCP`。如果服务器开启了系统防火墙，也需要放行该端口。

停止服务：

```bash
docker compose down
```

只停止服务但保留数据库数据。若执行 `docker compose down -v`，会删除 MySQL 数据卷，业务数据会被清空。

### 方式一：便携本地模式

如果当前电脑没有 MySQL，直接使用 H2 本地数据库启动：

```powershell
.\start-anywhere.ps1 -Port 8080
```

脚本会先打包项目，再使用 `local` 配置启动系统，并打印：

```text
Local URL: http://localhost:8080/
LAN URL:   http://本机局域网IP:8080/
```

同一 Wi-Fi 或同一局域网中的其他设备，可以访问 `LAN URL`。如果访问失败，需要在 Windows 防火墙中允许 Java 或 8080 端口入站。

### 方式二：正式 MySQL 模式

在服务器、实验室电脑或自己的电脑上使用 MySQL 时，可以这样启动：

```powershell
$env:SERVER_ADDRESS='0.0.0.0'
$env:SERVER_PORT='8080'
mvn package -DskipTests
java -jar target/airport-flight-simulation-0.0.1-SNAPSHOT.jar
```

访问地址：

```text
本机访问：http://localhost:8080/
局域网访问：http://本机局域网IP:8080/
```

如果希望不在同一网络也能访问，需要把项目部署到云服务器，或使用内网穿透工具将本机端口映射到公网地址。代码已经支持监听外部网卡，但公网访问还取决于服务器、防火墙、路由器和域名配置。

## 启动命令

开发运行：

```powershell
mvn spring-boot:run
```

如果当前机器运行 `mvn spring-boot:run` 出现 classpath 异常，使用更稳定的打包运行方式：

```powershell
mvn package -DskipTests
java -jar target/airport-flight-simulation-0.0.1-SNAPSHOT.jar
```

也可以先打包再运行：

```powershell
mvn package -DskipTests
java -jar target/airport-flight-simulation-0.0.1-SNAPSHOT.jar
```

默认端口为 `8080`，默认监听地址为 `0.0.0.0`。启动后访问：

```text
http://localhost:8080
```

## 页面入口

| 页面 | 地址 | 说明 |
| --- | --- | --- |
| 登录页 | `http://localhost:8080/` | 系统入口，演示账号 `admin`，密码 `123456` |
| 首页 | `http://localhost:8080/dashboard` | 系统仪表盘和关键统计 |
| 航班管理 | `http://localhost:8080/flights` | 航班列表、组合筛选、新增、编辑、删除保护 |
| 跑道管理 | `http://localhost:8080/runways` | 跑道列表、新增、编辑、删除保护和占用状态 |
| 停机位管理 | `http://localhost:8080/gates` | 停机位列表、新增、编辑、删除保护和占用时间 |
| 仿真运行 | `http://localhost:8080/simulation` | 创建仿真任务、单步推进、自动运行、重置、等待队列和资源状态 |
| 仿真日志 | `http://localhost:8080/records` | 按任务、航班和事件类型查看仿真事件记录 |
| 统计结果 | `http://localhost:8080/statistics` | 查看航班、资源利用率和延误统计 |

## 测试命令

运行全部测试：

```powershell
mvn test
```

只运行 Web Controller 测试：

```powershell
mvn test '-Dtest=WebControllerTest'
```


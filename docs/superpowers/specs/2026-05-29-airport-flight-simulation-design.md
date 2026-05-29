# 机场航班运行仿真系统设计说明

## 1. 项目定位

本项目采用标准版 Web 系统方案，技术栈为 Java 17、Spring Boot、Maven、MySQL、Thymeleaf 和 JUnit 5。目标是完成一个可运行、可演示、可测试、可写入课程实验文档的机场航班运行仿真系统。

系统重点体现软件工程实验流程：需求分析、概要设计、编码实现、测试验收和项目交付。实现范围控制在标准版，不包含登录、天气影响、复杂图表、多调度算法对比等增强功能。

## 2. 功能范围

标准版需要完成以下功能：

1. 首页仪表盘：展示当前仿真时间、总航班数、已完成航班数、延误航班数、空闲跑道数、空闲停机位数。
2. 航班管理：新增、编辑、删除、查询航班，支持按航班号和状态筛选。
3. 跑道管理：新增、编辑、维护跑道状态，展示当前占用航班。
4. 停机位管理：新增、编辑、维护停机位状态，展示当前占用航班。
5. 仿真控制：创建仿真任务、单步推进、自动运行、重置仿真。
6. 仿真日志：记录并展示航班进入等待、资源分配、起飞、降落、延误、完成等事件。
7. 统计结果：展示完成数量、延误数量、平均延误、最大延误和资源使用情况。

不纳入第一版实现的功能：

1. 用户登录和权限管理。
2. 天气影响。
3. 复杂图表展示。
4. 多调度算法对比。
5. 紧急航班特殊优先策略。

这些功能可作为报告中的后续扩展方向。

## 3. 系统架构

系统采用经典三层架构：

```text
Thymeleaf 页面层
        ↓
Controller 控制层
        ↓
Service 业务层
        ↓
Repository 数据访问层
        ↓
MySQL 数据库
```

各层职责如下：

1. 页面层：展示表格、表单、仿真控制按钮、日志和统计结果。
2. Controller 层：接收页面请求，调用业务服务，返回页面视图或重定向。
3. Service 层：实现基础数据管理、仿真时间推进、调度算法、日志记录和统计计算。
4. Repository 层：通过 Spring Data JPA 访问 MySQL。
5. 数据库层：持久化航班、跑道、停机位、仿真任务和仿真记录。

## 4. 页面设计

系统包含 7 个主要页面：

1. `/`：首页仪表盘。
2. `/flights`：航班管理页面。
3. `/runways`：跑道管理页面。
4. `/gates`：停机位管理页面。
5. `/simulation`：仿真控制页面。
6. `/records`：仿真日志页面。
7. `/statistics`：统计结果页面。

页面风格采用课程项目适合的表格型后台界面，强调清晰、稳定、便于截图，不追求复杂前端效果。

## 5. 数据库设计

数据库名称为 `airport_simulation`。第一版保留 5 张核心表。

### 5.1 航班表 flight

用途：保存航班计划和运行状态。

字段：

```text
id
flight_no
airline
departure_airport
arrival_airport
flight_type
aircraft_type
planned_departure_time
planned_arrival_time
actual_departure_time
actual_arrival_time
status
delay_minutes
priority
created_time
updated_time
```

### 5.2 跑道表 runway

用途：保存跑道信息和占用状态。

字段：

```text
id
runway_no
status
support_type
current_flight_id
occupied_start_time
occupied_end_time
created_time
updated_time
```

### 5.3 停机位表 gate

用途：保存停机位信息和占用状态。

字段：

```text
id
gate_no
status
aircraft_type
current_flight_id
created_time
updated_time
```

### 5.4 仿真任务表 simulation_task

用途：保存一次仿真任务的配置和当前进度。

字段：

```text
id
task_name
start_time
end_time
time_step_minutes
current_time
status
created_time
updated_time
```

### 5.5 仿真记录表 simulation_record

用途：保存仿真过程中的事件日志。

字段：

```text
id
task_id
flight_id
event_time
event_type
description
created_time
```

## 6. 枚举设计

航班类型 `FlightType`：

```text
DEPARTURE
ARRIVAL
```

机型 `AircraftType`：

```text
SMALL
MEDIUM
LARGE
```

航班状态 `FlightStatus`：

```text
SCHEDULED
WAITING
RUNNING
COMPLETED
DELAYED
CANCELLED
```

资源状态 `ResourceStatus`：

```text
IDLE
OCCUPIED
MAINTENANCE
```

跑道支持类型 `RunwaySupportType`：

```text
TAKEOFF_ONLY
LANDING_ONLY
BOTH
```

仿真任务状态 `SimulationTaskStatus`：

```text
CREATED
RUNNING
FINISHED
RESET
```

仿真事件类型 `SimulationEventType`：

```text
ENTER_QUEUE
ASSIGN_RUNWAY
ASSIGN_GATE
TAKEOFF
LANDING
DELAY
COMPLETE
RELEASE_RESOURCE
```

## 7. 核心仿真流程

系统采用离散时间步仿真。用户可以在页面上单步推进，也可以自动运行完整仿真。

单次推进流程：

1. 读取当前仿真任务的 `current_time`。
2. 释放已到占用结束时间的跑道。
3. 查询计划时间小于等于 `current_time` 且未完成、未取消的航班。
4. 将符合条件的航班置为等待状态。
5. 按调度规则排序等待航班。
6. 依次尝试分配跑道和停机位。
7. 分配成功时更新航班、跑道、停机位状态，并写入仿真日志。
8. 分配失败时累计延误时间，并写入延误日志。
9. 将仿真任务的 `current_time` 增加一个时间步。
10. 如果超过结束时间，将任务状态置为 `FINISHED`。

## 8. 调度规则

第一版采用 FCFS 先到先服务规则，并加入简单优先级排序：

1. 计划时间越早，越优先处理。
2. 同一计划时间下，降落航班优先于起飞航班。
3. 同一计划时间和类型下，`priority` 数值越大越优先。
4. 起飞航班需要可用跑道。
5. 降落航班需要可用跑道和适配机型的空闲停机位。
6. 没有资源时航班继续等待，并累计延误时间。
7. 跑道占用时间统一为一个时间步，下一步开始时释放。
8. 降落航班完成后，停机位保持占用，用于展示资源使用情况。

## 9. 统计指标

统计页面需要展示：

1. 总航班数。
2. 起飞航班数。
3. 降落航班数。
4. 已完成航班数。
5. 延误航班数。
6. 平均延误时间。
7. 最大延误时间。
8. 空闲跑道数。
9. 空闲停机位数。
10. 当前等待航班数。

## 10. 测试范围

第一版测试重点包括：

1. 航班、跑道、停机位基础 CRUD。
2. 仿真任务创建。
3. 单步推进后航班状态变化。
4. 有资源时正确分配跑道和停机位。
5. 资源不足时正确累计延误。
6. 仿真日志正确生成。
7. 统计结果计算正确。

核心调度逻辑需要编写 JUnit 单元测试。页面功能可通过手工测试和运行截图验证。

## 11. 交付物

最终交付目录建议包含：

```text
source-code
database/airport_simulation.sql
docs/需求分析说明书.docx
docs/概要设计说明书.docx
docs/测试设计及结果报告书.docx
screenshots
README.md
项目演示PPT.pptx
```

开发阶段优先完成源码、数据库脚本和 README，之后再整理正式课程文档、截图和 PPT。

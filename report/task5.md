# 机场航班运行仿真系统 Task 5 开发记录

记录时间：2026-05-29
项目路径：`D:\Desktop\airport-flight-simulation`
任务名称：实现仿真和统计 Service

## 1. 任务目标

Task 5 的目标是在基础 CRUD Service 之后，实现系统核心业务能力：

1. 创建仿真任务。
2. 按离散时间步推进仿真。
3. 按 FCFS 规则调度航班。
4. 分配跑道和停机位资源。
5. 记录仿真事件日志。
6. 汇总航班和资源统计数据。

## 2. 新增文件

```text
src/main/java/com/example/airportsimulation/service/SimulationService.java
src/main/java/com/example/airportsimulation/service/StatisticsService.java
src/main/java/com/example/airportsimulation/service/StatisticsSummary.java
src/test/java/com/example/airportsimulation/service/SimulationServiceTest.java
src/test/java/com/example/airportsimulation/service/StatisticsServiceTest.java
```

## 3. SimulationService

主要方法：

```java
SimulationTask createTask(SimulationTask task)
SimulationTask getLatestTask()
SimulationTask step(Long taskId)
SimulationTask runAll(Long taskId)
void reset()
```

### 3.1 创建任务

`createTask` 会校验：

1. `taskName` 不能为空。
2. `startTime` 不能为空。
3. `endTime` 不能为空。
4. `endTime` 不能早于 `startTime`。
5. `timeStepMinutes` 必须大于 0。

创建时会调用实体已有的 `prepareForCreate()`，保证 `currentTime` 默认等于 `startTime`。

### 3.2 单步推进算法

`step` 的核心流程：

1. 读取仿真任务当前时间。
2. 释放占用结束时间小于等于当前时间的跑道。
3. 查找状态为 `SCHEDULED`、`WAITING`、`DELAYED` 且计划时间小于等于当前时间的航班。
4. 按计划时间升序排序。
5. 同一计划时间下，降落航班优先于起飞航班。
6. 同一计划时间和航班类型下，`priority` 越大越优先。
7. 起飞航班需要空闲且支持起飞的跑道。
8. 降落航班需要空闲且支持降落的跑道，以及匹配机型的空闲停机位。
9. 分配成功后，航班状态设为 `COMPLETED`，写入分配和完成记录。
10. 分配失败后，航班状态设为 `DELAYED`，延误分钟数增加一个时间步，写入延误记录。
11. 推进 `currentTime`。
12. 如果当前时间超过结束时间，任务状态设为 `FINISHED`。

### 3.3 事件记录

当前会记录以下事件类型：

1. `ENTER_QUEUE`
2. `ASSIGN_RUNWAY`
3. `ASSIGN_GATE`
4. `TAKEOFF`
5. `LANDING`
6. `DELAY`
7. `COMPLETE`
8. `RELEASE_RESOURCE`

## 4. StatisticsService

主要方法：

```java
StatisticsSummary getSummary()
```

`StatisticsSummary` 字段：

```java
long totalFlights
long departureFlights
long arrivalFlights
long completedFlights
long delayedFlights
double averageDelayMinutes
int maxDelayMinutes
long idleRunways
long idleGates
long waitingFlights
```

统计来源：

1. 航班总数、起飞数、降落数来自 `flight` 表。
2. 完成、延误、等待数量根据航班状态统计。
3. 平均延误和最大延误根据 `delayMinutes` 统计。
4. 空闲跑道和空闲停机位根据资源状态 `IDLE` 统计。

## 5. 测试说明

新增测试类：

```text
SimulationServiceTest
StatisticsServiceTest
```

### 5.1 SimulationServiceTest

覆盖用例：

1. 一个起飞航班和一条空闲跑道，可以完成起飞并占用跑道。
2. 一个降落航班、一条空闲跑道和一个匹配停机位，可以完成降落并占用停机位。
3. 没有可用跑道时，航班状态变为 `DELAYED`，延误分钟数增加一个时间步。
4. 同一计划时间下，降落航班优先于起飞航班。

### 5.2 StatisticsServiceTest

覆盖用例：

1. 统计航班总数、起飞数、降落数、完成数、延误数、等待数。
2. 统计平均延误和最大延误。
3. 统计空闲跑道和空闲停机位数量。

## 6. 测试先行过程

先新增测试并执行：

```powershell
mvn test '-Dtest=SimulationServiceTest,StatisticsServiceTest'
```

首次执行结果为编译失败，原因是以下类尚未实现：

```text
SimulationService
StatisticsService
StatisticsSummary
```

这符合测试先行预期。

随后实现 Service 和统计 DTO，再次执行定向测试：

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

最后执行完整测试：

```powershell
mvn test
```

最终结果：

```text
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 7. 当前状态

Task 5 已完成。项目现在具备基础仿真推进、FCFS 调度、资源分配、事件记录和统计汇总能力，可以继续 Task 6：Controller 和 Thymeleaf 页面。

# 机场航班运行仿真系统 Task 4 开发记录

记录时间：2026-05-29
项目路径：`D:\Desktop\airport-flight-simulation`
任务名称：实现航班、跑道、停机位基础 CRUD Service

## 1. 任务目标

Task 4 的目标是在已有 Entity 和 Repository 基础上补充 Service 业务层，为后续 Controller 页面和仿真核心逻辑提供统一入口。

本次完成三个基础管理服务：

1. `FlightService`
2. `RunwayService`
3. `GateService`

## 2. 新增文件

```text
src/main/java/com/example/airportsimulation/service/FlightService.java
src/main/java/com/example/airportsimulation/service/RunwayService.java
src/main/java/com/example/airportsimulation/service/GateService.java
src/test/java/com/example/airportsimulation/service/BasicCrudServiceTest.java
```

## 3. FlightService

主要方法：

```java
List<Flight> findAll(String keyword, FlightStatus status)
Flight findById(Long id)
Flight save(Flight flight)
void deleteById(Long id)
```

业务规则：

1. `flightNo`、`flightType`、`aircraftType` 不能为空。
2. 起飞航班必须填写 `plannedDepartureTime`。
3. 降落航班必须填写 `plannedArrivalTime`。
4. 新航班默认状态为 `SCHEDULED`。
5. 延误分钟数和优先级小于 0 时按 0 处理。
6. 查询支持按航班号关键字或航班状态筛选。

## 4. RunwayService

主要方法：

```java
List<Runway> findAll()
Runway findById(Long id)
Runway save(Runway runway)
void deleteById(Long id)
```

业务规则：

1. `runwayNo` 不能为空。
2. 新跑道默认状态为 `IDLE`。
3. 新跑道默认支持类型为 `BOTH`。
4. ID 不存在时抛出 `NoSuchElementException`。

## 5. GateService

主要方法：

```java
List<Gate> findAll()
Gate findById(Long id)
Gate save(Gate gate)
void deleteById(Long id)
```

业务规则：

1. `gateNo` 不能为空。
2. `aircraftType` 不能为空。
3. 新停机位默认状态为 `IDLE`。
4. ID 不存在时抛出 `NoSuchElementException`。

## 6. 测试说明

新增测试类：

```text
src/test/java/com/example/airportsimulation/service/BasicCrudServiceTest.java
```

测试覆盖：

1. 航班保存、默认值设置、ID 查询、关键字查询、状态查询。
2. 航班必填字段和计划时间校验。
3. 跑道保存、默认值设置、列表查询、删除、ID 不存在异常。
4. 跑道编号必填校验。
5. 停机位保存、默认值设置、列表查询、删除、ID 不存在异常。
6. 停机位编号和机型必填校验。

## 7. 验证结果

先执行测试，确认因 Service 不存在而编译失败，符合测试先行预期。

随后实现 Service，再次执行：

```powershell
mvn test
```

最终结果：

```text
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 8. 当前状态

Task 4 已完成。项目现在具备基础业务服务层，可以继续开发 Task 5：仿真和统计 Service。

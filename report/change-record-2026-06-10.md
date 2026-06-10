# 2026-06-10 项目更新记录

## 本次更新范围

1. 前端整体 UI 改造为 Apple / Linear / Stripe 倾向的 Bento Box 架构。
2. 全局导航由左侧长边栏调整为顶部极简导航，保留全部功能入口。
3. 运行概览页重构为 Bento 仪表盘，机场态势图作为核心大卡片。
4. 机场运行态势图从纯 CSS 线框升级为“机场鸟瞰底图 + 飞机图标 + 数据标注层”。
5. 保留地图图层切换、热力图、冲突线、缩放/平移、右键菜单、时间倍速等原有交互。
6. 航班、跑道、停机位、仿真、日志、统计、数据交换等页面统一表格、表单、按钮、状态标签和移动端 viewport。
7. 修复 CSV 导出在 Excel/WPS 中打开中文乱码的问题，为导出内容追加 UTF-8 BOM。
8. 新增 CSV 导出编码回归测试。
9. 生成可导入的航班计划样例文件。

## 新增文件

- `flight-plan-import-sample.csv`：36 条航班计划，可在“数据交换”页面直接导入。
- `src/main/resources/static/img/airport-surface-map.png`：机场鸟瞰运行底图。
- `src/main/resources/static/img/aircraft-top.png`：俯视飞机图标。
- `src/test/java/com/example/airportsimulation/controller/OperationsControllerTest.java`：CSV 导出 BOM 编码测试。
- `report/ui-bento.md`：Bento UI 改造说明。
- `report/change-record-2026-06-10.md`：本次更新记录。

## 重点修改文件

- `src/main/resources/templates/fragments/header.html`
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/static/css/app.css`
- `src/main/resources/static/js/app.js`
- `src/main/java/com/example/airportsimulation/controller/OperationsController.java`
- `src/main/resources/templates/*.html`

## 航班计划导入格式

导入文件字段如下：

```csv
flightNo,airline,departureAirport,arrivalAirport,flightType,aircraftType,plannedTime
```

字段约束：

- `flightType`：`ARRIVAL` 或 `DEPARTURE`
- `aircraftType`：`SMALL`、`MEDIUM` 或 `LARGE`
- `plannedTime`：ISO 本地时间格式，例如 `2026-06-10T08:10`

## 验证记录

本次提交前执行：

- `mvn test`
- `mvn package -DskipTests`
- CSV 样例结构校验：36 行数据，枚举和时间格式校验通过。

## 运行入口

本地启动后访问：

- `http://127.0.0.1:8080/dashboard`
- `http://127.0.0.1:8080/operations/data`

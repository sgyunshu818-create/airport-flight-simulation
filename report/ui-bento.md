# UI Bento 改造报告

## 改造目标

本次改造将机场航班运行仿真系统从偏重的 HUD/panel 风格，调整为 Apple / Linear / Stripe 倾向的 Bento Box 信息架构。核心目标是减少边框和发光堆叠，用深色纯色卡片、统一圆角、稳定网格和更清晰的数据层级提升产品感。

## 设计方案

1. 全局导航从左侧长边栏改为顶部 Global Header，保留所有原有页面入口。
2. 首页改为 Bento Grid：机场运行态势图作为 C 位大卡片，周围环绕今日航班、延误率、资源负载、延误队列、时间轴、仿真操作和事件流。
3. 普通管理页保留原有 Thymeleaf 模板和表单逻辑，通过全局 CSS 将 panel、metric、table、filter、form 统一成 Bento 卡片语言。
4. 弱化边框和外发光，改用纯色卡片底、留白、等宽数字和低饱和状态色。
5. 保留现有地图交互：图层切换、缩放/平移、右键菜单、时间倍速按钮和键盘快捷键。
6. 机场运行态势图进一步改为位图底图与数据标注层组合，使用机场鸟瞰 PNG 和俯视飞机 PNG 替代纯线框表现。

## 页面覆盖

- 运行概览：首页 Bento Dashboard 与机场态势图重构。
- 航班、跑道、停机位：顶部导航、筛选区、表格、操作按钮统一为 Bento 风格。
- 仿真控制：指标卡、测试场景、任务卡、资源态势图和日志区统一视觉。
- 统计分析、事件日志、数据交换、冲突/异常/排班/场景/角色页面：统一容器、卡片、表格、按钮与移动端 viewport。

## 关键文件

- `src/main/resources/templates/fragments/header.html`：全局导航从侧边栏改为顶部导航。
- `src/main/resources/templates/dashboard.html`：首页改为 Bento Grid 架构。
- `src/main/resources/static/css/app.css`：新增 Bento 视觉系统、响应式断点和全局组件覆盖。
- `src/main/resources/static/js/app.js`：导航高亮支持新的顶部导航。
- `src/main/resources/static/img/airport-surface-map.png`：机场鸟瞰运行底图。
- `src/main/resources/static/img/aircraft-top.png`：俯视飞机图标，用于航班标记和停机位标注。
- `src/main/resources/templates/*.html`：为主要页面补充 viewport meta，改善移动端布局。

## 验证结果

- `mvn test`：通过，32 个测试全部成功。
- `mvn package -DskipTests`：通过。
- 本地服务：已启动并验证 `/dashboard`、`/flights`、`/simulation`、`/operations/data`、`/statistics`、`/css/app.css`、`/js/app.js` 均返回 HTTP 200。
- 浏览器截图验证：当前 Codex 运行时的 `playwright` 包缺少 `playwright-core`，自动截图未能执行；已改用 HTTP 页面渲染入口和测试构建进行验证。

## 注意事项

本次改造以不破坏后端接口、表单字段、按钮行为和路由为前提。CSV 导出中文乱码修复仍保留在当前工作区中，未被 UI 改造覆盖。

# Task 6 Web Pages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add complete Spring MVC controllers, Thymeleaf pages, CSS, tests, and a development report for Task 6.

**Architecture:** Keep controllers thin and delegate business rules to the existing Service layer. Thymeleaf templates provide simple admin-style tables and forms, while CSS supplies the shared visual style.

**Tech Stack:** Java 17, Spring Boot MVC, Thymeleaf, JUnit 5, Mockito, Maven.

---

## File Structure

Create:

1. `src/main/java/com/example/airportsimulation/controller/DashboardController.java`
2. `src/main/java/com/example/airportsimulation/controller/StatisticsController.java`
3. `src/main/java/com/example/airportsimulation/controller/FlightController.java`
4. `src/main/java/com/example/airportsimulation/controller/RunwayController.java`
5. `src/main/java/com/example/airportsimulation/controller/GateController.java`
6. `src/main/java/com/example/airportsimulation/controller/SimulationController.java`
7. `src/main/java/com/example/airportsimulation/controller/RecordController.java`
8. `src/main/resources/templates/dashboard.html`
9. `src/main/resources/templates/statistics.html`
10. `src/main/resources/templates/flights.html`
11. `src/main/resources/templates/flight-form.html`
12. `src/main/resources/templates/runways.html`
13. `src/main/resources/templates/runway-form.html`
14. `src/main/resources/templates/gates.html`
15. `src/main/resources/templates/gate-form.html`
16. `src/main/resources/templates/simulation.html`
17. `src/main/resources/templates/records.html`
18. `src/main/resources/static/css/app.css`
19. `src/test/java/com/example/airportsimulation/controller/WebControllerTest.java`
20. `report/task6.md`

Modify no existing production service classes unless tests reveal a compile issue.

## Task 1: MVC Test Coverage

- [ ] Create `WebControllerTest.java` using `@WebMvcTest` with mocked `FlightService`, `RunwayService`, `GateService`, `SimulationService`, `StatisticsService`, and `SimulationRecordRepository`.
- [ ] Add tests for dashboard, statistics, list pages, form pages, simulation page, simulation actions, records page, and flight create redirect.
- [ ] Run `mvn test '-Dtest=WebControllerTest'`.
- [ ] Expected first result: compilation failure because controller classes do not exist.

## Task 2: Controllers

- [ ] Create all controller classes listed in the file structure.
- [ ] Use constructor injection.
- [ ] Put enum values into form models where needed.
- [ ] Catch form errors and return the current form template with `error`.
- [ ] Run `mvn test '-Dtest=WebControllerTest'`.
- [ ] Expected result: controller tests compile and fail only if templates or model names are wrong.

## Task 3: Thymeleaf Pages and CSS

- [ ] Create all templates listed in the file structure.
- [ ] Add compact navigation to each page.
- [ ] Add `app.css` with restrained admin styling for navigation, forms, tables, status labels, and action buttons.
- [ ] Run `mvn test '-Dtest=WebControllerTest'`.
- [ ] Expected result: targeted MVC tests pass.

## Task 4: Full Verification and Report

- [ ] Run `mvn test`.
- [ ] Fix any test or compile failures.
- [ ] Create `report/task6.md` with task goal, added files, route list, page descriptions, testing process, and final test result.
- [ ] Run `mvn test` again after adding the report.
- [ ] Expected result: full test suite passes.

## Self-Review

Spec coverage:

1. Dashboard, CRUD pages, simulation controls, records, statistics, CSS, tests, and report each have a task.
2. The plan does not include excluded features such as login, charts, REST APIs, or additional algorithms.

Placeholder scan:

1. The plan contains no deferred implementation placeholders.
2. Every file to create is listed explicitly.

Type consistency:

1. Controller names and template names match the design.
2. Service names match the existing project classes.
3. Test command uses Maven and the existing project layout.

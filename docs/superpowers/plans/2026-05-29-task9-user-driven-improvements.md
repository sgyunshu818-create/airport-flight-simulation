# Task 9 User-Driven Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve high-frequency usability for simulation, filtering, logs, statistics, navigation maintenance, delete safety, and documentation.

**Architecture:** Keep the current Spring Boot MVC + Thymeleaf structure. Add focused repository query helpers, service methods, minimal entity fields for gate occupancy, reusable Thymeleaf header fragments, and tests around each behavior.

**Tech Stack:** Java 17, Spring Boot 3.3.5, Spring MVC, Spring Data JPA, Thymeleaf, JUnit 5, AssertJ.

---

### Task 1: Gate Occupancy Release

**Files:**
- Modify: `src/main/java/com/example/airportsimulation/entity/Gate.java`
- Modify: `src/main/java/com/example/airportsimulation/service/SimulationService.java`
- Modify: `database/airport_simulation.sql`
- Test: `src/test/java/com/example/airportsimulation/service/SimulationServiceTest.java`

- [ ] Add `occupiedStartTime` and `occupiedEndTime` to `Gate`.
- [ ] In arrival assignment, set gate occupancy start/end time.
- [ ] At each simulation step, release gates whose end time is not after current time.
- [ ] Reset gate occupancy fields in `reset()`.
- [ ] Add test proving a gate is released on the next step and a release record is written.

### Task 2: Filtering and Logs

**Files:**
- Modify: `FlightRepository.java`, `FlightService.java`, `FlightController.java`, `flights.html`
- Modify: `SimulationRecordRepository.java`, `RecordController.java`, `records.html`
- Test: `BasicCrudServiceTest.java`, `RepositoryQueryTest.java`, `WebControllerTest.java`

- [ ] Add combined flight filtering: keyword, status, flight type, airline.
- [ ] Add log filtering: task id, flight id, event type.
- [ ] Update pages to expose filters and preserve selected values.
- [ ] Add tests for combined filters and controller model attributes.

### Task 3: User-Facing Explainability and Stats

**Files:**
- Modify: `SimulationController.java`, `simulation.html`
- Modify: `StatisticsSummary.java`, `StatisticsService.java`, `dashboard.html`, `statistics.html`
- Test: `StatisticsServiceTest.java`, `WebControllerTest.java`

- [ ] Show waiting/delayed flight queue on simulation page.
- [ ] Show resource status tables on simulation page.
- [ ] Show recent records on simulation page.
- [ ] Add completion rate, delay rate, runway occupancy rate, gate occupancy rate.

### Task 4: Validation and Delete Protection

**Files:**
- Modify: `FlightService.java`, `RunwayService.java`, `GateService.java`
- Modify: `FlightController.java`, `RunwayController.java`, `GateController.java`
- Modify: list templates for error display.
- Test: `BasicCrudServiceTest.java`, `WebControllerTest.java`

- [ ] Convert main validation errors to Chinese messages.
- [ ] Prevent deleting completed/running/waiting/delayed flights.
- [ ] Prevent deleting occupied runways/gates.
- [ ] Surface delete errors on list pages.

### Task 5: Navigation Fragment and Documentation

**Files:**
- Create: `templates/fragments/header.html`
- Modify: all page templates.
- Modify: `README.md`
- Create: `report/task9.md`

- [ ] Extract repeated topbar into a Thymeleaf fragment.
- [ ] Replace duplicated headers across pages.
- [ ] Update README for `/` login and `/dashboard` overview.
- [ ] Add report entry and final verification notes.

### Verification

- [ ] Run targeted tests for each changed test class.
- [ ] Run `mvn test`.
- [ ] Verify running page responses for `/`, `/dashboard`, `/flights`, `/simulation`, `/records`, `/statistics`.

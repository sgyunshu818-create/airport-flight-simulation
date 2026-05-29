# Airport Flight Simulation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a standard Spring Boot Web system for airport flight simulation with CRUD pages, MySQL persistence, discrete-time scheduling, logs, statistics, and basic tests.

**Architecture:** Use a layered Spring Boot application: Thymeleaf pages call controllers, controllers call services, services use Spring Data JPA repositories, and MySQL stores persistent data. The simulation engine lives in service code and uses FCFS scheduling with a simple priority tiebreaker.

**Tech Stack:** Java 17, Spring Boot 3.3.x, Maven, Spring Data JPA, MySQL, Thymeleaf, Bootstrap-free custom CSS, JUnit 5, H2 for tests.

---

## File Structure

Create this project structure:

```text
pom.xml
README.md
database/airport_simulation.sql
src/main/java/com/example/airportsimulation/AirportSimulationApplication.java
src/main/java/com/example/airportsimulation/controller/DashboardController.java
src/main/java/com/example/airportsimulation/controller/FlightController.java
src/main/java/com/example/airportsimulation/controller/RunwayController.java
src/main/java/com/example/airportsimulation/controller/GateController.java
src/main/java/com/example/airportsimulation/controller/SimulationController.java
src/main/java/com/example/airportsimulation/controller/RecordController.java
src/main/java/com/example/airportsimulation/controller/StatisticsController.java
src/main/java/com/example/airportsimulation/entity/Flight.java
src/main/java/com/example/airportsimulation/entity/Runway.java
src/main/java/com/example/airportsimulation/entity/Gate.java
src/main/java/com/example/airportsimulation/entity/SimulationTask.java
src/main/java/com/example/airportsimulation/entity/SimulationRecord.java
src/main/java/com/example/airportsimulation/enums/AircraftType.java
src/main/java/com/example/airportsimulation/enums/FlightStatus.java
src/main/java/com/example/airportsimulation/enums/FlightType.java
src/main/java/com/example/airportsimulation/enums/ResourceStatus.java
src/main/java/com/example/airportsimulation/enums/RunwaySupportType.java
src/main/java/com/example/airportsimulation/enums/SimulationEventType.java
src/main/java/com/example/airportsimulation/enums/SimulationTaskStatus.java
src/main/java/com/example/airportsimulation/repository/FlightRepository.java
src/main/java/com/example/airportsimulation/repository/RunwayRepository.java
src/main/java/com/example/airportsimulation/repository/GateRepository.java
src/main/java/com/example/airportsimulation/repository/SimulationTaskRepository.java
src/main/java/com/example/airportsimulation/repository/SimulationRecordRepository.java
src/main/java/com/example/airportsimulation/service/FlightService.java
src/main/java/com/example/airportsimulation/service/RunwayService.java
src/main/java/com/example/airportsimulation/service/GateService.java
src/main/java/com/example/airportsimulation/service/SimulationService.java
src/main/java/com/example/airportsimulation/service/StatisticsService.java
src/main/java/com/example/airportsimulation/service/dto/StatisticsSummary.java
src/main/resources/application.yml
src/main/resources/templates/layout.html
src/main/resources/templates/dashboard.html
src/main/resources/templates/flights.html
src/main/resources/templates/flight-form.html
src/main/resources/templates/runways.html
src/main/resources/templates/runway-form.html
src/main/resources/templates/gates.html
src/main/resources/templates/gate-form.html
src/main/resources/templates/simulation.html
src/main/resources/templates/records.html
src/main/resources/templates/statistics.html
src/main/resources/static/css/app.css
src/test/java/com/example/airportsimulation/service/SimulationServiceTest.java
src/test/java/com/example/airportsimulation/service/StatisticsServiceTest.java
src/test/resources/application-test.yml
```

Responsibilities:

1. `entity`: JPA table mappings only.
2. `enums`: stable domain constants used by entities, forms, and services.
3. `repository`: Spring Data query methods.
4. `service`: business rules, validation, simulation, and statistics.
5. `controller`: HTTP routing and model preparation.
6. `templates`: Thymeleaf pages.
7. `database`: SQL script for manual MySQL setup and sample data.

---

### Task 1: Maven Project Skeleton

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/example/airportsimulation/AirportSimulationApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/test/resources/application-test.yml`

- [ ] **Step 1: Create `pom.xml`**

Use Spring Boot 3.3.5, Java 17, and these dependencies:

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

- [ ] **Step 2: Create the application entry point**

```java
package com.example.airportsimulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AirportSimulationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AirportSimulationApplication.class, args);
    }
}
```

- [ ] **Step 3: Configure MySQL in `application.yml`**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/airport_simulation?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  thymeleaf:
    cache: false

server:
  port: 8080
```

- [ ] **Step 4: Configure test database in `application-test.yml`**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:airport_simulation_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
```

- [ ] **Step 5: Verify project compiles**

Run:

```powershell
mvn test
```

Expected: build succeeds with no tests run or with default context checks passing.

- [ ] **Step 6: Commit**

```powershell
git add pom.xml src/main/java src/main/resources src/test/resources
git commit -m "chore: create spring boot project skeleton"
```

---

### Task 2: Domain Enums and Entities

**Files:**
- Create all files under `src/main/java/com/example/airportsimulation/enums`
- Create all files under `src/main/java/com/example/airportsimulation/entity`

- [ ] **Step 1: Create enum classes**

Each enum contains only the constants from the design spec:

```java
public enum FlightType { DEPARTURE, ARRIVAL }
public enum AircraftType { SMALL, MEDIUM, LARGE }
public enum FlightStatus { SCHEDULED, WAITING, RUNNING, COMPLETED, DELAYED, CANCELLED }
public enum ResourceStatus { IDLE, OCCUPIED, MAINTENANCE }
public enum RunwaySupportType { TAKEOFF_ONLY, LANDING_ONLY, BOTH }
public enum SimulationTaskStatus { CREATED, RUNNING, FINISHED, RESET }
public enum SimulationEventType {
    ENTER_QUEUE, ASSIGN_RUNWAY, ASSIGN_GATE, TAKEOFF, LANDING, DELAY, COMPLETE, RELEASE_RESOURCE
}
```

- [ ] **Step 2: Create `Flight` entity**

Fields: `id`, `flightNo`, `airline`, `departureAirport`, `arrivalAirport`, `flightType`, `aircraftType`, `plannedDepartureTime`, `plannedArrivalTime`, `actualDepartureTime`, `actualArrivalTime`, `status`, `delayMinutes`, `priority`, `createdTime`, `updatedTime`.

Use `@Enumerated(EnumType.STRING)` for enums, `@Column(nullable = false)` for required fields, and `@PrePersist/@PreUpdate` for timestamps. Initialize `status = SCHEDULED`, `delayMinutes = 0`, and `priority = 0`.

- [ ] **Step 3: Create `Runway` entity**

Fields: `id`, `runwayNo`, `status`, `supportType`, `currentFlightId`, `occupiedStartTime`, `occupiedEndTime`, `createdTime`, `updatedTime`.

Initialize `status = IDLE` and `supportType = BOTH`.

- [ ] **Step 4: Create `Gate` entity**

Fields: `id`, `gateNo`, `status`, `aircraftType`, `currentFlightId`, `createdTime`, `updatedTime`.

Initialize `status = IDLE`.

- [ ] **Step 5: Create `SimulationTask` entity**

Fields: `id`, `taskName`, `startTime`, `endTime`, `timeStepMinutes`, `currentTime`, `status`, `createdTime`, `updatedTime`.

Initialize `status = CREATED`, and when creating a task set `currentTime = startTime`.

- [ ] **Step 6: Create `SimulationRecord` entity**

Fields: `id`, `taskId`, `flightId`, `eventTime`, `eventType`, `description`, `createdTime`.

- [ ] **Step 7: Compile**

Run:

```powershell
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit**

```powershell
git add src/main/java/com/example/airportsimulation/enums src/main/java/com/example/airportsimulation/entity
git commit -m "feat: add airport simulation domain model"
```

---

### Task 3: Repositories and SQL Script

**Files:**
- Create repositories under `src/main/java/com/example/airportsimulation/repository`
- Create: `database/airport_simulation.sql`

- [ ] **Step 1: Create repositories**

Repository interfaces:

```java
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByFlightNoContainingIgnoreCase(String flightNo);
    List<Flight> findByStatus(FlightStatus status);
}
```

```java
public interface RunwayRepository extends JpaRepository<Runway, Long> {
    Optional<Runway> findByRunwayNo(String runwayNo);
    List<Runway> findByStatus(ResourceStatus status);
}
```

```java
public interface GateRepository extends JpaRepository<Gate, Long> {
    Optional<Gate> findByGateNo(String gateNo);
    List<Gate> findByStatus(ResourceStatus status);
}
```

```java
public interface SimulationTaskRepository extends JpaRepository<SimulationTask, Long> {
    Optional<SimulationTask> findTopByOrderByCreatedTimeDesc();
}
```

```java
public interface SimulationRecordRepository extends JpaRepository<SimulationRecord, Long> {
    List<SimulationRecord> findByTaskIdOrderByEventTimeAscIdAsc(Long taskId);
    List<SimulationRecord> findAllByOrderByEventTimeDescIdDesc();
}
```

- [ ] **Step 2: Create SQL script**

`database/airport_simulation.sql` must:

1. Create database `airport_simulation`.
2. Create the five core tables.
3. Insert sample runways `R01`, `R02`.
4. Insert sample gates `G01`, `G02`, `G03`.
5. Insert 8 sample flights with overlapping planned times.

- [ ] **Step 3: Verify repositories compile**

Run:

```powershell
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```powershell
git add src/main/java/com/example/airportsimulation/repository database/airport_simulation.sql
git commit -m "feat: add repositories and database script"
```

---

### Task 4: Basic CRUD Services

**Files:**
- Create: `FlightService.java`
- Create: `RunwayService.java`
- Create: `GateService.java`

- [ ] **Step 1: Implement `FlightService`**

Methods:

```java
List<Flight> findAll(String keyword, FlightStatus status)
Flight findById(Long id)
Flight save(Flight flight)
void deleteById(Long id)
```

Rules:

1. `flightNo`, `flightType`, `aircraftType` must be present.
2. For departure flights, `plannedDepartureTime` must be present.
3. For arrival flights, `plannedArrivalTime` must be present.
4. New flights default to `SCHEDULED`, `delayMinutes = 0`, `priority = 0`.

- [ ] **Step 2: Implement `RunwayService`**

Methods:

```java
List<Runway> findAll()
Runway findById(Long id)
Runway save(Runway runway)
void deleteById(Long id)
```

Rules:

1. `runwayNo` is required.
2. New runways default to `IDLE`.
3. `supportType` defaults to `BOTH`.

- [ ] **Step 3: Implement `GateService`**

Methods:

```java
List<Gate> findAll()
Gate findById(Long id)
Gate save(Gate gate)
void deleteById(Long id)
```

Rules:

1. `gateNo` and `aircraftType` are required.
2. New gates default to `IDLE`.

- [ ] **Step 4: Compile**

Run:

```powershell
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/example/airportsimulation/service
git commit -m "feat: add basic management services"
```

---

### Task 5: Simulation and Statistics Services with Tests

**Files:**
- Create: `SimulationService.java`
- Create: `StatisticsService.java`
- Create: `StatisticsSummary.java`
- Create: `SimulationServiceTest.java`
- Create: `StatisticsServiceTest.java`

- [ ] **Step 1: Write `SimulationServiceTest`**

Test cases:

1. `stepAssignsDepartureToIdleRunway`: one departure flight and one idle runway produce `COMPLETED`.
2. `stepAssignsArrivalToRunwayAndGate`: one arrival flight, one runway, one matching gate produce `COMPLETED` and gate becomes `OCCUPIED`.
3. `stepDelaysFlightWhenRunwayUnavailable`: no idle runway increases `delayMinutes` by `timeStepMinutes` and status becomes `DELAYED`.
4. `arrivalHasPriorityWhenPlannedTimeEquals`: one arrival and one departure at the same time with one runway processes arrival first.

- [ ] **Step 2: Write `StatisticsServiceTest`**

Test cases:

1. completed, delayed, and total counts are calculated from saved flights.
2. average delay and max delay are calculated from `delayMinutes`.
3. idle runway and idle gate counts are calculated from resource tables.

- [ ] **Step 3: Run tests and verify failure**

Run:

```powershell
mvn test -Dtest=SimulationServiceTest,StatisticsServiceTest
```

Expected: fail because services are not implemented.

- [ ] **Step 4: Implement `SimulationService`**

Methods:

```java
SimulationTask createTask(SimulationTask task)
SimulationTask getLatestTask()
SimulationTask step(Long taskId)
SimulationTask runAll(Long taskId)
void reset()
```

`step` algorithm:

1. Load task.
2. Release runways where `occupiedEndTime <= currentTime`.
3. Find pending flights with planned time <= current time and status in `SCHEDULED`, `WAITING`, `DELAYED`.
4. Sort by planned time ascending, arrival before departure, priority descending.
5. For each flight, try to allocate resources.
6. If allocation succeeds, write resource assignment and completion records.
7. If allocation fails, add `timeStepMinutes` to delay and write `DELAY`.
8. Advance `currentTime`.
9. Mark task `FINISHED` if current time is after end time.

- [ ] **Step 5: Implement `StatisticsService`**

Return `StatisticsSummary` with:

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

- [ ] **Step 6: Run tests and verify success**

Run:

```powershell
mvn test -Dtest=SimulationServiceTest,StatisticsServiceTest
```

Expected: all tests pass.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/example/airportsimulation/service src/test/java/com/example/airportsimulation/service
git commit -m "feat: add simulation and statistics services"
```

---

### Task 6: Controllers and Thymeleaf Pages

**Files:**
- Create all controllers under `controller`
- Create templates under `src/main/resources/templates`
- Create CSS under `src/main/resources/static/css/app.css`

- [ ] **Step 1: Create dashboard and statistics controllers**

Routes:

```text
GET / -> dashboard.html
GET /statistics -> statistics.html
```

Both use `StatisticsService.getSummary()`.

- [ ] **Step 2: Create CRUD controllers**

Routes:

```text
GET /flights
GET /flights/new
POST /flights
GET /flights/{id}/edit
POST /flights/{id}
POST /flights/{id}/delete
```

Repeat the same pattern for `/runways` and `/gates`.

- [ ] **Step 3: Create simulation controllers**

Routes:

```text
GET /simulation
POST /simulation/tasks
POST /simulation/tasks/{id}/step
POST /simulation/tasks/{id}/run
POST /simulation/reset
GET /records
```

- [ ] **Step 4: Create shared layout and CSS**

`layout.html` provides navigation links to all pages. `app.css` uses a restrained dashboard style with tables, compact forms, and clear action buttons.

- [ ] **Step 5: Create Thymeleaf pages**

Pages:

1. `dashboard.html`: cards for statistics and links to main actions.
2. `flights.html` and `flight-form.html`.
3. `runways.html` and `runway-form.html`.
4. `gates.html` and `gate-form.html`.
5. `simulation.html`: task form and step/run/reset buttons.
6. `records.html`: event log table.
7. `statistics.html`: summary table.

- [ ] **Step 6: Compile**

Run:

```powershell
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/example/airportsimulation/controller src/main/resources/templates src/main/resources/static
git commit -m "feat: add web controllers and thymeleaf pages"
```

---

### Task 7: README and Manual Verification

**Files:**
- Create or modify: `README.md`

- [ ] **Step 1: Write README**

Include:

1. Project introduction.
2. Tech stack.
3. Environment requirements.
4. MySQL database initialization.
5. `application.yml` database account instructions.
6. Run command.
7. Page list.
8. Suggested demo flow.

- [ ] **Step 2: Run full test suite**

Run:

```powershell
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Start local server**

Run:

```powershell
mvn spring-boot:run
```

Expected:

```text
Tomcat started on port 8080
Started AirportSimulationApplication
```

- [ ] **Step 4: Manual browser verification**

Open:

```text
http://localhost:8080
```

Verify:

1. Dashboard loads.
2. Flight list loads.
3. Runway list loads.
4. Gate list loads.
5. Simulation page can create a task.
6. Step button creates records.
7. Statistics page updates.

- [ ] **Step 5: Commit**

```powershell
git add README.md
git commit -m "docs: add project run instructions"
```

---

## Self-Review

Spec coverage:

1. Functional pages are covered in Task 6.
2. Database and entities are covered in Tasks 2 and 3.
3. Simulation algorithm is covered in Task 5.
4. Statistics are covered in Task 5 and Task 6.
5. Tests are covered in Task 5 and Task 7.
6. README and database script are covered in Tasks 3 and 7.

Placeholder scan:

1. The plan contains no unfinished placeholder markers.
2. Enhancement items are explicitly excluded from the first implementation.

Type consistency:

1. Entity, enum, repository, service, controller, and template names use the same package and naming scheme.
2. `currentFlightId` is stored as a scalar ID in resource entities to keep the standard version simple.
3. Status and event names match the design document.

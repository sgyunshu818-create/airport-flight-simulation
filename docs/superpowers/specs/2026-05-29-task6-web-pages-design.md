# Task 6 Web Pages Design

## Goal

Task 6 adds the web presentation layer for the airport flight simulation system. The system should be usable from a browser for course demonstration, with complete but simple management pages for flights, runways, gates, simulation control, records, and statistics.

## Scope

Included:

1. Dashboard page at `/`.
2. Flight CRUD pages with keyword and status filters.
3. Runway CRUD pages.
4. Gate CRUD pages.
5. Simulation page for creating a task, stepping, running all, and resetting.
6. Record page for simulation event logs.
7. Statistics page for summary metrics.
8. Shared CSS with a restrained admin-style layout.
9. MVC tests for page routing and form actions.
10. Task 6 development report.

Excluded:

1. User login.
2. Complex charts.
3. Frontend framework integration.
4. REST API endpoints.
5. Additional simulation algorithms.

## Architecture

Controllers are thin routing and model-preparation classes. They call the existing Service layer and repositories, then return Thymeleaf template names. Business validation remains in Service classes.

Thymeleaf pages use plain HTML forms and tables. CSS lives in `src/main/resources/static/css/app.css`. Navigation is repeated directly in each page through a small header block to keep the standard version simple and avoid introducing layout-fragment complexity.

## Routes

1. `GET /` renders `dashboard.html`.
2. `GET /statistics` renders `statistics.html`.
3. `GET /flights` renders `flights.html`.
4. `GET /flights/new` renders `flight-form.html`.
5. `POST /flights` creates a flight.
6. `GET /flights/{id}/edit` renders `flight-form.html`.
7. `POST /flights/{id}` updates a flight.
8. `POST /flights/{id}/delete` deletes a flight.
9. Runway routes follow the same pattern under `/runways`.
10. Gate routes follow the same pattern under `/gates`.
11. `GET /simulation` renders `simulation.html`.
12. `POST /simulation/tasks` creates a simulation task.
13. `POST /simulation/tasks/{id}/step` advances one time step.
14. `POST /simulation/tasks/{id}/run` runs until finished.
15. `POST /simulation/reset` resets simulation state.
16. `GET /records` renders `records.html`.

## Error Handling

Form submissions catch `IllegalArgumentException` and `NoSuchElementException`, then re-render the same page with an `error` message. Successful submissions redirect to the relevant list or simulation page.

If there is no simulation task yet, `/simulation` shows an empty task form and no step or run controls. `/records` still shows an empty table.

## Testing

MVC tests use `@WebMvcTest` and mocked services/repositories. Tests cover:

1. Dashboard page loads with statistics.
2. Flight list and new form routes load.
3. Flight create action redirects to `/flights`.
4. Runway and gate list pages load.
5. Simulation page loads without an existing task.
6. Simulation task create and step actions redirect to `/simulation`.
7. Records and statistics pages load.

The final verification command is `mvn test`.

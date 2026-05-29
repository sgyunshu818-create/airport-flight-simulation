package com.example.airportsimulation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.SimulationRecord;
import com.example.airportsimulation.entity.SimulationTask;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.enums.SimulationEventType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RepositoryQueryTest {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SimulationTaskRepository simulationTaskRepository;

    @Autowired
    private SimulationRecordRepository simulationRecordRepository;

    @Test
    void findsFlightsByFlightNoKeywordAndStatus() {
        Flight flight = new Flight();
        flight.setFlightNo("MU5101");
        flight.setAirline("China Eastern");
        flight.setDepartureAirport("SHA");
        flight.setArrivalAirport("PEK");
        flight.setFlightType(FlightType.DEPARTURE);
        flight.setAircraftType(AircraftType.MEDIUM);
        flight.setPlannedDepartureTime(LocalDateTime.of(2026, 6, 1, 8, 0));
        flight.setStatus(FlightStatus.WAITING);
        flightRepository.save(flight);

        assertThat(flightRepository.findByFlightNoContainingIgnoreCase("mu51"))
                .extracting(Flight::getFlightNo)
                .containsExactly("MU5101");
        assertThat(flightRepository.findByStatus(FlightStatus.WAITING))
                .extracting(Flight::getFlightNo)
                .containsExactly("MU5101");
    }

    @Test
    void findsLatestTaskAndOrderedRecords() {
        SimulationTask olderTask = createTask("上午仿真", LocalDateTime.of(2026, 6, 1, 8, 0));
        SimulationTask latestTask = createTask("下午仿真", LocalDateTime.of(2026, 6, 1, 14, 0));
        simulationTaskRepository.save(olderTask);
        simulationTaskRepository.save(latestTask);

        SimulationRecord second = createRecord(latestTask.getId(), LocalDateTime.of(2026, 6, 1, 14, 10), "second");
        SimulationRecord first = createRecord(latestTask.getId(), LocalDateTime.of(2026, 6, 1, 14, 0), "first");
        simulationRecordRepository.save(second);
        simulationRecordRepository.save(first);

        assertThat(simulationTaskRepository.findTopByOrderByCreatedTimeDesc())
                .isPresent()
                .get()
                .extracting(SimulationTask::getTaskName)
                .isEqualTo("下午仿真");
        assertThat(simulationRecordRepository.findByTaskIdOrderByEventTimeAscIdAsc(latestTask.getId()))
                .extracting(SimulationRecord::getDescription)
                .containsExactly("first", "second");
    }

    private SimulationTask createTask(String name, LocalDateTime startTime) {
        SimulationTask task = new SimulationTask();
        task.setTaskName(name);
        task.setStartTime(startTime);
        task.setEndTime(startTime.plusHours(2));
        task.setTimeStepMinutes(10);
        return task;
    }

    private SimulationRecord createRecord(Long taskId, LocalDateTime eventTime, String description) {
        SimulationRecord record = new SimulationRecord();
        record.setTaskId(taskId);
        record.setEventTime(eventTime);
        record.setEventType(SimulationEventType.ENTER_QUEUE);
        record.setDescription(description);
        return record;
    }
}

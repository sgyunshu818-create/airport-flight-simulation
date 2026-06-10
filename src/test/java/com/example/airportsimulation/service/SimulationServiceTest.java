package com.example.airportsimulation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.entity.SimulationRecord;
import com.example.airportsimulation.entity.SimulationTask;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
import com.example.airportsimulation.enums.SimulationEventType;
import com.example.airportsimulation.repository.FlightRepository;
import com.example.airportsimulation.repository.GateRepository;
import com.example.airportsimulation.repository.RunwayRepository;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import com.example.airportsimulation.repository.SimulationTaskRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SimulationService.class)
@ActiveProfiles("test")
class SimulationServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 6, 1, 8, 0);

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private RunwayRepository runwayRepository;

    @Autowired
    private GateRepository gateRepository;

    @Autowired
    private SimulationTaskRepository taskRepository;

    @Autowired
    private SimulationRecordRepository recordRepository;

    @Test
    void stepAssignsDepartureToIdleRunway() {
        SimulationTask task = simulationService.createTask(task("morning"));
        Flight departure = flightRepository.save(departure("MU5101", START));
        runwayRepository.save(runway("R01", RunwaySupportType.TAKEOFF_ONLY, ResourceStatus.IDLE));

        simulationService.step(task.getId());

        Flight saved = flightRepository.findById(departure.getId()).orElseThrow();
        Runway runway = runwayRepository.findAll().get(0);
        assertThat(saved.getStatus()).isEqualTo(FlightStatus.COMPLETED);
        assertThat(saved.getActualDepartureTime()).isEqualTo(START);
        assertThat(runway.getStatus()).isEqualTo(ResourceStatus.OCCUPIED);
        assertThat(runway.getCurrentFlightId()).isEqualTo(saved.getId());
        assertThat(recordRepository.findByTaskIdOrderByEventTimeAscIdAsc(task.getId()))
                .extracting(SimulationRecord::getEventType)
                .contains(SimulationEventType.ASSIGN_RUNWAY, SimulationEventType.TAKEOFF, SimulationEventType.COMPLETE);
    }

    @Test
    void stepAssignsArrivalToRunwayAndGate() {
        SimulationTask task = simulationService.createTask(task("arrival"));
        Flight arrival = flightRepository.save(arrival("CA1202", START, AircraftType.LARGE));
        runwayRepository.save(runway("R01", RunwaySupportType.LANDING_ONLY, ResourceStatus.IDLE));
        gateRepository.save(gate("G01", AircraftType.LARGE, ResourceStatus.IDLE));

        simulationService.step(task.getId());

        Flight saved = flightRepository.findById(arrival.getId()).orElseThrow();
        Gate gate = gateRepository.findAll().get(0);
        assertThat(saved.getStatus()).isEqualTo(FlightStatus.COMPLETED);
        assertThat(saved.getActualArrivalTime()).isEqualTo(START);
        assertThat(gate.getStatus()).isEqualTo(ResourceStatus.OCCUPIED);
        assertThat(gate.getCurrentFlightId()).isEqualTo(saved.getId());
        assertThat(recordRepository.findByTaskIdOrderByEventTimeAscIdAsc(task.getId()))
                .extracting(SimulationRecord::getEventType)
                .contains(SimulationEventType.ASSIGN_RUNWAY, SimulationEventType.ASSIGN_GATE,
                        SimulationEventType.LANDING, SimulationEventType.COMPLETE);
    }

    @Test
    void stepDelaysFlightWhenRunwayUnavailable() {
        SimulationTask task = simulationService.createTask(task("delay"));
        Flight departure = flightRepository.save(departure("MU5101", START));
        runwayRepository.save(runway("R01", RunwaySupportType.BOTH, ResourceStatus.MAINTENANCE));

        simulationService.step(task.getId());

        Flight saved = flightRepository.findById(departure.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(FlightStatus.DELAYED);
        assertThat(saved.getDelayMinutes()).isEqualTo(10);
        assertThat(recordRepository.findByTaskIdOrderByEventTimeAscIdAsc(task.getId()))
                .extracting(SimulationRecord::getEventType)
                .contains(SimulationEventType.DELAY);
    }

    @Test
    void arrivalHasPriorityWhenPlannedTimeEquals() {
        SimulationTask task = simulationService.createTask(task("priority"));
        Flight departure = flightRepository.save(departure("MU5101", START));
        Flight arrival = flightRepository.save(arrival("CA1202", START, AircraftType.MEDIUM));
        runwayRepository.save(runway("R01", RunwaySupportType.BOTH, ResourceStatus.IDLE));
        gateRepository.save(gate("G01", AircraftType.MEDIUM, ResourceStatus.IDLE));

        simulationService.step(task.getId());

        Flight savedArrival = flightRepository.findById(arrival.getId()).orElseThrow();
        Flight savedDeparture = flightRepository.findById(departure.getId()).orElseThrow();
        assertThat(savedArrival.getStatus()).isEqualTo(FlightStatus.COMPLETED);
        assertThat(savedDeparture.getStatus()).isEqualTo(FlightStatus.DELAYED);
    }


    @Test
    void stepReleasesGateWhenOccupiedTimeEnds() {
        SimulationTask task = simulationService.createTask(task("gate release"));
        Flight arrival = flightRepository.save(arrival("CA1301", START, AircraftType.LARGE));
        runwayRepository.save(runway("R01", RunwaySupportType.LANDING_ONLY, ResourceStatus.IDLE));
        gateRepository.save(gate("G01", AircraftType.LARGE, ResourceStatus.IDLE));

        simulationService.step(task.getId());
        Gate occupiedGate = gateRepository.findAll().get(0);
        assertThat(occupiedGate.getStatus()).isEqualTo(ResourceStatus.OCCUPIED);
        assertThat(occupiedGate.getCurrentFlightId()).isEqualTo(arrival.getId());
        assertThat(occupiedGate.getOccupiedEndTime()).isEqualTo(START.plusMinutes(10));

        simulationService.step(task.getId());

        Gate releasedGate = gateRepository.findAll().get(0);
        assertThat(releasedGate.getStatus()).isEqualTo(ResourceStatus.IDLE);
        assertThat(releasedGate.getCurrentFlightId()).isNull();
        assertThat(releasedGate.getOccupiedStartTime()).isNull();
        assertThat(releasedGate.getOccupiedEndTime()).isNull();
        assertThat(recordRepository.findByTaskIdOrderByEventTimeAscIdAsc(task.getId()))
                .extracting(SimulationRecord::getEventType)
                .contains(SimulationEventType.RELEASE_RESOURCE);
    }
    private SimulationTask task(String name) {
        SimulationTask task = new SimulationTask();
        task.setTaskName(name);
        task.setStartTime(START);
        task.setEndTime(START.plusHours(1));
        task.setTimeStepMinutes(10);
        return task;
    }

    private Flight departure(String flightNo, LocalDateTime plannedTime) {
        Flight flight = new Flight();
        flight.setFlightNo(flightNo);
        flight.setFlightType(FlightType.DEPARTURE);
        flight.setAircraftType(AircraftType.MEDIUM);
        flight.setDepartureAirport("SHA");
        flight.setArrivalAirport("PEK");
        flight.setPlannedDepartureTime(plannedTime);
        return flight;
    }

    private Flight arrival(String flightNo, LocalDateTime plannedTime, AircraftType aircraftType) {
        Flight flight = new Flight();
        flight.setFlightNo(flightNo);
        flight.setFlightType(FlightType.ARRIVAL);
        flight.setAircraftType(aircraftType);
        flight.setDepartureAirport("PEK");
        flight.setArrivalAirport("SHA");
        flight.setPlannedArrivalTime(plannedTime);
        return flight;
    }

    private Runway runway(String runwayNo, RunwaySupportType supportType, ResourceStatus status) {
        Runway runway = new Runway();
        runway.setRunwayNo(runwayNo);
        runway.setSupportType(supportType);
        runway.setStatus(status);
        return runway;
    }

    private Gate gate(String gateNo, AircraftType aircraftType, ResourceStatus status) {
        Gate gate = new Gate();
        gate.setGateNo(gateNo);
        gate.setAircraftType(aircraftType);
        gate.setStatus(status);
        return gate;
    }
}


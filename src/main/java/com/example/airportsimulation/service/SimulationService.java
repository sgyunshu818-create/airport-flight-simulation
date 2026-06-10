package com.example.airportsimulation.service;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.entity.SimulationRecord;
import com.example.airportsimulation.entity.SimulationTask;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
import com.example.airportsimulation.enums.SimulationEventType;
import com.example.airportsimulation.enums.SimulationTaskStatus;
import com.example.airportsimulation.repository.FlightRepository;
import com.example.airportsimulation.repository.GateRepository;
import com.example.airportsimulation.repository.RunwayRepository;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import com.example.airportsimulation.repository.SimulationTaskRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class SimulationService {
    private final SimulationTaskRepository taskRepository;
    private final SimulationRecordRepository recordRepository;
    private final FlightRepository flightRepository;
    private final RunwayRepository runwayRepository;
    private final GateRepository gateRepository;

    public SimulationService(
            SimulationTaskRepository taskRepository,
            SimulationRecordRepository recordRepository,
            FlightRepository flightRepository,
            RunwayRepository runwayRepository,
            GateRepository gateRepository) {
        this.taskRepository = taskRepository;
        this.recordRepository = recordRepository;
        this.flightRepository = flightRepository;
        this.runwayRepository = runwayRepository;
        this.gateRepository = gateRepository;
    }

    public SimulationTask createTask(SimulationTask task) {
        validateTask(task);
        task.prepareForCreate();
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public SimulationTask getLatestTask() {
        return taskRepository.findTopByOrderByCreatedTimeDescIdDesc()
                .orElseThrow(() -> new NoSuchElementException("No simulation task found"));
    }

    public SimulationTask step(Long taskId) {
        SimulationTask task = findTask(taskId);
        LocalDateTime currentTime = task.getCurrentTime();
        int stepMinutes = task.getTimeStepMinutes();

        releaseFinishedRunways(task, currentTime);
        releaseFinishedGates(task, currentTime);
        List<Flight> pendingFlights = findPendingFlights(currentTime);
        for (Flight flight : pendingFlights) {
            if (flight.getStatus() == FlightStatus.SCHEDULED) {
                flight.setStatus(FlightStatus.WAITING);
                saveRecord(task, flight, SimulationEventType.ENTER_QUEUE, flight.getFlightNo() + " enters queue");
            }
            if (!tryCompleteFlight(task, flight, currentTime, stepMinutes)) {
                delayFlight(task, flight, stepMinutes);
            }
        }

        task.setCurrentTime(currentTime.plusMinutes(stepMinutes));
        if (task.getCurrentTime().isAfter(task.getEndTime())) {
            task.setStatus(SimulationTaskStatus.FINISHED);
        } else {
            task.setStatus(SimulationTaskStatus.RUNNING);
        }
        return taskRepository.save(task);
    }

    public SimulationTask runAll(Long taskId) {
        SimulationTask task = findTask(taskId);
        int guard = 0;
        while (task.getStatus() != SimulationTaskStatus.FINISHED && guard < 1000) {
            task = step(task.getId());
            guard++;
        }
        return task;
    }

    public void reset() {
        recordRepository.deleteAll();
        taskRepository.deleteAll();

        for (Flight flight : flightRepository.findAll()) {
            flight.setStatus(FlightStatus.SCHEDULED);
            flight.setDelayMinutes(0);
            flight.setActualDepartureTime(null);
            flight.setActualArrivalTime(null);
            flightRepository.save(flight);
        }
        for (Runway runway : runwayRepository.findAll()) {
            runway.setStatus(ResourceStatus.IDLE);
            runway.setCurrentFlightId(null);
            runway.setOccupiedStartTime(null);
            runway.setOccupiedEndTime(null);
            runwayRepository.save(runway);
        }
        for (Gate gate : gateRepository.findAll()) {
            gate.setStatus(ResourceStatus.IDLE);
            gate.setCurrentFlightId(null);
            gate.setOccupiedStartTime(null);
            gate.setOccupiedEndTime(null);
            gateRepository.save(gate);
        }
    }

    private SimulationTask findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Simulation task not found: " + taskId));
    }

    private void validateTask(SimulationTask task) {
        if (task == null) {
            throw new IllegalArgumentException("task is required");
        }
        if (!StringUtils.hasText(task.getTaskName())) {
            throw new IllegalArgumentException("taskName is required");
        }
        if (task.getStartTime() == null) {
            throw new IllegalArgumentException("startTime is required");
        }
        if (task.getEndTime() == null) {
            throw new IllegalArgumentException("endTime is required");
        }
        if (task.getEndTime().isBefore(task.getStartTime())) {
            throw new IllegalArgumentException("endTime must not be before startTime");
        }
        if (task.getTimeStepMinutes() <= 0) {
            throw new IllegalArgumentException("timeStepMinutes must be positive");
        }
    }

    private void releaseFinishedRunways(SimulationTask task, LocalDateTime currentTime) {
        runwayRepository.findAll().stream()
                .filter(runway -> runway.getStatus() == ResourceStatus.OCCUPIED)
                .filter(runway -> runway.getOccupiedEndTime() != null)
                .filter(runway -> !runway.getOccupiedEndTime().isAfter(currentTime))
                .forEach(runway -> {
                    runway.setStatus(ResourceStatus.IDLE);
                    runway.setCurrentFlightId(null);
                    runway.setOccupiedStartTime(null);
                    runway.setOccupiedEndTime(null);
                    runwayRepository.save(runway);
                    saveRecord(task, null, SimulationEventType.RELEASE_RESOURCE,
                            runway.getRunwayNo() + " released");
                });
    }


    private void releaseFinishedGates(SimulationTask task, LocalDateTime currentTime) {
        gateRepository.findAll().stream()
                .filter(gate -> gate.getStatus() == ResourceStatus.OCCUPIED)
                .filter(gate -> gate.getOccupiedEndTime() != null)
                .filter(gate -> !gate.getOccupiedEndTime().isAfter(currentTime))
                .forEach(gate -> {
                    gate.setStatus(ResourceStatus.IDLE);
                    gate.setCurrentFlightId(null);
                    gate.setOccupiedStartTime(null);
                    gate.setOccupiedEndTime(null);
                    gateRepository.save(gate);
                    saveRecord(task, null, SimulationEventType.RELEASE_RESOURCE,
                            gate.getGateNo() + " released");
                });
    }
    private List<Flight> findPendingFlights(LocalDateTime currentTime) {
        return flightRepository.findAll().stream()
                .filter(flight -> flight.getStatus() == FlightStatus.SCHEDULED
                        || flight.getStatus() == FlightStatus.WAITING
                        || flight.getStatus() == FlightStatus.DELAYED)
                .filter(flight -> !plannedTime(flight).isAfter(currentTime))
                .sorted(Comparator
                        .comparing(this::plannedTime)
                        .thenComparing(flight -> flight.getFlightType() == FlightType.ARRIVAL ? 0 : 1)
                        .thenComparing(Comparator.comparingInt(Flight::getPriority).reversed()))
                .toList();
    }

    private boolean tryCompleteFlight(
            SimulationTask task,
            Flight flight,
            LocalDateTime currentTime,
            int stepMinutes) {
        if (flight.getFlightType() == FlightType.DEPARTURE) {
            return tryCompleteDeparture(task, flight, currentTime, stepMinutes);
        }
        return tryCompleteArrival(task, flight, currentTime, stepMinutes);
    }

    private boolean tryCompleteDeparture(
            SimulationTask task,
            Flight flight,
            LocalDateTime currentTime,
            int stepMinutes) {
        Runway runway = findIdleRunwayForDeparture();
        if (runway == null) {
            return false;
        }
        occupyRunway(runway, flight, currentTime, stepMinutes);
        flight.setActualDepartureTime(currentTime);
        flight.setStatus(FlightStatus.COMPLETED);
        flightRepository.save(flight);
        saveRecord(task, flight, SimulationEventType.ASSIGN_RUNWAY,
                flight.getFlightNo() + " assigned runway " + runway.getRunwayNo());
        saveRecord(task, flight, SimulationEventType.TAKEOFF,
                flight.getFlightNo() + " takeoff completed");
        saveRecord(task, flight, SimulationEventType.COMPLETE,
                flight.getFlightNo() + " completed");
        return true;
    }

    private boolean tryCompleteArrival(
            SimulationTask task,
            Flight flight,
            LocalDateTime currentTime,
            int stepMinutes) {
        Runway runway = findIdleRunwayForArrival();
        Gate gate = findIdleGateFor(flight);
        if (runway == null || gate == null) {
            return false;
        }
        occupyRunway(runway, flight, currentTime, stepMinutes);
        gate.setStatus(ResourceStatus.OCCUPIED);
        gate.setCurrentFlightId(flight.getId());
        gate.setOccupiedStartTime(currentTime);
        gate.setOccupiedEndTime(currentTime.plusMinutes(stepMinutes));
        gateRepository.save(gate);
        flight.setActualArrivalTime(currentTime);
        flight.setStatus(FlightStatus.COMPLETED);
        flightRepository.save(flight);
        saveRecord(task, flight, SimulationEventType.ASSIGN_RUNWAY,
                flight.getFlightNo() + " assigned runway " + runway.getRunwayNo());
        saveRecord(task, flight, SimulationEventType.ASSIGN_GATE,
                flight.getFlightNo() + " assigned gate " + gate.getGateNo());
        saveRecord(task, flight, SimulationEventType.LANDING,
                flight.getFlightNo() + " landing completed");
        saveRecord(task, flight, SimulationEventType.COMPLETE,
                flight.getFlightNo() + " completed");
        return true;
    }

    private Runway findIdleRunwayForDeparture() {
        return runwayRepository.findAll().stream()
                .filter(runway -> runway.getStatus() == ResourceStatus.IDLE)
                .filter(runway -> runway.getSupportType() == RunwaySupportType.TAKEOFF_ONLY
                        || runway.getSupportType() == RunwaySupportType.BOTH)
                .findFirst()
                .orElse(null);
    }

    private Runway findIdleRunwayForArrival() {
        return runwayRepository.findAll().stream()
                .filter(runway -> runway.getStatus() == ResourceStatus.IDLE)
                .filter(runway -> runway.getSupportType() == RunwaySupportType.LANDING_ONLY
                        || runway.getSupportType() == RunwaySupportType.BOTH)
                .findFirst()
                .orElse(null);
    }

    private Gate findIdleGateFor(Flight flight) {
        return gateRepository.findAll().stream()
                .filter(gate -> gate.getStatus() == ResourceStatus.IDLE)
                .filter(gate -> gate.getAircraftType() == flight.getAircraftType())
                .findFirst()
                .orElse(null);
    }

    private void occupyRunway(Runway runway, Flight flight, LocalDateTime currentTime, int stepMinutes) {
        runway.setStatus(ResourceStatus.OCCUPIED);
        runway.setCurrentFlightId(flight.getId());
        runway.setOccupiedStartTime(currentTime);
        runway.setOccupiedEndTime(currentTime.plusMinutes(stepMinutes));
        runwayRepository.save(runway);
    }

    private void delayFlight(SimulationTask task, Flight flight, int stepMinutes) {
        flight.setStatus(FlightStatus.DELAYED);
        flight.setDelayMinutes(flight.getDelayMinutes() + stepMinutes);
        flightRepository.save(flight);
        saveRecord(task, flight, SimulationEventType.DELAY,
                flight.getFlightNo() + " delayed " + flight.getDelayMinutes() + " minutes");
    }

    private LocalDateTime plannedTime(Flight flight) {
        if (flight.getFlightType() == FlightType.ARRIVAL) {
            return flight.getPlannedArrivalTime();
        }
        return flight.getPlannedDepartureTime();
    }

    private void saveRecord(
            SimulationTask task,
            Flight flight,
            SimulationEventType eventType,
            String description) {
        SimulationRecord record = new SimulationRecord();
        record.setTaskId(task.getId());
        record.setFlightId(flight == null ? null : flight.getId());
        record.setEventTime(task.getCurrentTime());
        record.setEventType(eventType);
        record.setDescription(description);
        recordRepository.save(record);
    }
}





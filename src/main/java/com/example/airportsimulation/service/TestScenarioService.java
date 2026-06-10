package com.example.airportsimulation.service;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.entity.SimulationTask;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
import com.example.airportsimulation.enums.SimulationTaskStatus;
import com.example.airportsimulation.repository.FlightRepository;
import com.example.airportsimulation.repository.GateRepository;
import com.example.airportsimulation.repository.RunwayRepository;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import com.example.airportsimulation.repository.SimulationTaskRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestScenarioService {
    private final FlightRepository flightRepository;
    private final RunwayRepository runwayRepository;
    private final GateRepository gateRepository;
    private final SimulationTaskRepository taskRepository;
    private final SimulationRecordRepository recordRepository;

    public TestScenarioService(
            FlightRepository flightRepository,
            RunwayRepository runwayRepository,
            GateRepository gateRepository,
            SimulationTaskRepository taskRepository,
            SimulationRecordRepository recordRepository) {
        this.flightRepository = flightRepository;
        this.runwayRepository = runwayRepository;
        this.gateRepository = gateRepository;
        this.taskRepository = taskRepository;
        this.recordRepository = recordRepository;
    }

    public String load(String scenarioKey) {
        resetRuntimeState();
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        return switch (scenarioKey) {
            case "runway-pressure" -> loadRunwayPressure(start, suffix);
            case "gate-shortage" -> loadGateShortage(start, suffix);
            case "priority-reorder" -> loadPriorityReorder(start, suffix);
            default -> loadBaseline(start, suffix);
        };
    }

    private String loadBaseline(LocalDateTime start, String suffix) {
        ensureRunways(2);
        ensureGates(List.of(AircraftType.SMALL, AircraftType.MEDIUM, AircraftType.LARGE, AircraftType.MEDIUM));
        createFlights("TBA" + suffix, start, List.of(
                spec("CA1357", FlightType.ARRIVAL, AircraftType.MEDIUM, 0, 2),
                spec("MU5532", FlightType.DEPARTURE, AircraftType.MEDIUM, 0, 1),
                spec("CZ3456", FlightType.ARRIVAL, AircraftType.SMALL, 5, 1),
                spec("HU7808", FlightType.DEPARTURE, AircraftType.LARGE, 10, 1),
                spec("MF8412", FlightType.ARRIVAL, AircraftType.MEDIUM, 15, 0),
                spec("SC4886", FlightType.DEPARTURE, AircraftType.SMALL, 20, 0)));
        createTask("测试-基准流量-" + suffix, start, start.plusMinutes(90), 5);
        return "已载入基准流量测试：6 个航班、2 条跑道、4 个停机位。";
    }

    private String loadRunwayPressure(LocalDateTime start, String suffix) {
        ensureRunways(1);
        ensureGates(List.of(AircraftType.SMALL, AircraftType.MEDIUM, AircraftType.LARGE));
        createFlights("TRP" + suffix, start, List.of(
                spec("MU2101", FlightType.DEPARTURE, AircraftType.MEDIUM, 0, 1),
                spec("CZ3305", FlightType.DEPARTURE, AircraftType.LARGE, 0, 2),
                spec("CA9820", FlightType.ARRIVAL, AircraftType.MEDIUM, 0, 1),
                spec("HO1258", FlightType.DEPARTURE, AircraftType.SMALL, 5, 0),
                spec("ZH9108", FlightType.ARRIVAL, AircraftType.LARGE, 5, 2),
                spec("MF8506", FlightType.DEPARTURE, AircraftType.MEDIUM, 10, 0),
                spec("3U8962", FlightType.ARRIVAL, AircraftType.MEDIUM, 10, 1)));
        createTask("测试-跑道拥堵-" + suffix, start, start.plusMinutes(100), 5);
        return "已载入跑道拥堵测试：单跑道承载密集进出港，适合观察延误队列。";
    }

    private String loadGateShortage(LocalDateTime start, String suffix) {
        ensureRunways(2);
        ensureGates(List.of(AircraftType.SMALL, AircraftType.MEDIUM));
        createFlights("TGS" + suffix, start, List.of(
                spec("CA4401", FlightType.ARRIVAL, AircraftType.LARGE, 0, 3),
                spec("MU7732", FlightType.ARRIVAL, AircraftType.LARGE, 0, 2),
                spec("CZ6009", FlightType.ARRIVAL, AircraftType.MEDIUM, 5, 1),
                spec("HU7602", FlightType.ARRIVAL, AircraftType.SMALL, 5, 1),
                spec("MF1908", FlightType.DEPARTURE, AircraftType.MEDIUM, 10, 0),
                spec("SC2206", FlightType.ARRIVAL, AircraftType.LARGE, 10, 2)));
        createTask("测试-机位不足-" + suffix, start, start.plusMinutes(90), 5);
        return "已载入机位不足测试：大型到港航班多于可用适配机位，适合观察冲突推荐。";
    }

    private String loadPriorityReorder(LocalDateTime start, String suffix) {
        ensureRunways(1);
        ensureGates(List.of(AircraftType.MEDIUM, AircraftType.MEDIUM, AircraftType.LARGE));
        createFlights("TPR" + suffix, start, List.of(
                spec("CA9999", FlightType.ARRIVAL, AircraftType.MEDIUM, 0, 9),
                spec("MU3001", FlightType.DEPARTURE, AircraftType.MEDIUM, 0, 1),
                spec("CZ3002", FlightType.DEPARTURE, AircraftType.MEDIUM, 0, 2),
                spec("HU3003", FlightType.ARRIVAL, AircraftType.LARGE, 5, 5),
                spec("MF3004", FlightType.DEPARTURE, AircraftType.MEDIUM, 5, 0),
                spec("SC3005", FlightType.ARRIVAL, AircraftType.MEDIUM, 10, 3)));
        createTask("测试-优先级插队-" + suffix, start, start.plusMinutes(90), 5);
        return "已载入优先级插队测试：高优先级航班与普通航班同窗运行，适合观察调度顺序。";
    }

    private void resetRuntimeState() {
        recordRepository.deleteAll();
        taskRepository.deleteAll();
        runwayRepository.findAll().forEach(runway -> {
            runway.setStatus(ResourceStatus.IDLE);
            runway.setCurrentFlightId(null);
            runway.setOccupiedStartTime(null);
            runway.setOccupiedEndTime(null);
            runwayRepository.save(runway);
        });
        gateRepository.findAll().forEach(gate -> {
            gate.setStatus(ResourceStatus.IDLE);
            gate.setCurrentFlightId(null);
            gate.setOccupiedStartTime(null);
            gate.setOccupiedEndTime(null);
            gateRepository.save(gate);
        });
    }

    private void ensureRunways(int count) {
        List<Runway> runways = runwayRepository.findAll();
        for (int i = runways.size() + 1; i <= count; i++) {
            Runway runway = new Runway();
            runway.setRunwayNo(i == 1 ? "36L/18R" : "36R/18L");
            runway.setStatus(ResourceStatus.IDLE);
            runway.setSupportType(RunwaySupportType.BOTH);
            runwayRepository.save(runway);
        }
    }

    private void ensureGates(List<AircraftType> aircraftTypes) {
        List<Gate> gates = gateRepository.findAll();
        for (int i = gates.size(); i < aircraftTypes.size(); i++) {
            Gate gate = new Gate();
            gate.setGateNo("T" + String.format("%02d", i + 1));
            gate.setAircraftType(aircraftTypes.get(i));
            gate.setStatus(ResourceStatus.IDLE);
            gateRepository.save(gate);
        }
    }

    private void createFlights(String prefix, LocalDateTime start, List<FlightSpec> specs) {
        int index = 1;
        for (FlightSpec spec : specs) {
            Flight flight = new Flight();
            flight.setFlightNo(prefix + "-" + index + "-" + spec.flightNo());
            flight.setAirline(airlineFor(spec.flightNo()));
            flight.setDepartureAirport(spec.flightType() == FlightType.DEPARTURE ? "天枢机场" : originFor(index));
            flight.setArrivalAirport(spec.flightType() == FlightType.ARRIVAL ? "天枢机场" : destinationFor(index));
            flight.setFlightType(spec.flightType());
            flight.setAircraftType(spec.aircraftType());
            if (spec.flightType() == FlightType.DEPARTURE) {
                flight.setPlannedDepartureTime(start.plusMinutes(spec.offsetMinutes()));
            } else {
                flight.setPlannedArrivalTime(start.plusMinutes(spec.offsetMinutes()));
            }
            flight.setStatus(FlightStatus.SCHEDULED);
            flight.setPriority(spec.priority());
            flightRepository.save(flight);
            index++;
        }
    }

    private void createTask(String name, LocalDateTime start, LocalDateTime end, int stepMinutes) {
        SimulationTask task = new SimulationTask();
        task.setTaskName(name);
        task.setStartTime(start);
        task.setCurrentTime(start);
        task.setEndTime(end);
        task.setTimeStepMinutes(stepMinutes);
        task.setStatus(SimulationTaskStatus.CREATED);
        taskRepository.save(task);
    }

    private FlightSpec spec(String flightNo, FlightType flightType, AircraftType aircraftType, int offsetMinutes, int priority) {
        return new FlightSpec(flightNo, flightType, aircraftType, offsetMinutes, priority);
    }

    private String airlineFor(String flightNo) {
        if (flightNo.startsWith("CA")) {
            return "中国国际航空";
        }
        if (flightNo.startsWith("MU")) {
            return "中国东方航空";
        }
        if (flightNo.startsWith("CZ")) {
            return "中国南方航空";
        }
        if (flightNo.startsWith("HU")) {
            return "海南航空";
        }
        return "测试航空";
    }

    private String originFor(int index) {
        return List.of("北京首都", "上海虹桥", "广州白云", "深圳宝安", "成都天府").get(index % 5);
    }

    private String destinationFor(int index) {
        return List.of("上海虹桥", "广州白云", "昆明长水", "厦门高崎", "杭州萧山").get(index % 5);
    }

    private record FlightSpec(String flightNo, FlightType flightType, AircraftType aircraftType, int offsetMinutes, int priority) {}
}

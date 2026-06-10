package com.example.airportsimulation.service;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.entity.SimulationRecord;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.repository.FlightRepository;
import com.example.airportsimulation.repository.GateRepository;
import com.example.airportsimulation.repository.RunwayRepository;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class OperationsInsightService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FlightRepository flightRepository;
    private final RunwayRepository runwayRepository;
    private final GateRepository gateRepository;
    private final SimulationRecordRepository simulationRecordRepository;
    private final StatisticsService statisticsService;

    public OperationsInsightService(
            FlightRepository flightRepository,
            RunwayRepository runwayRepository,
            GateRepository gateRepository,
            SimulationRecordRepository simulationRecordRepository,
            StatisticsService statisticsService) {
        this.flightRepository = flightRepository;
        this.runwayRepository = runwayRepository;
        this.gateRepository = gateRepository;
        this.simulationRecordRepository = simulationRecordRepository;
        this.statisticsService = statisticsService;
    }

    public List<ResourceConflict> findConflicts() {
        List<ResourceConflict> conflicts = new ArrayList<>();
        List<Flight> flights = flightRepository.findAll();
        List<Runway> runways = runwayRepository.findAll();
        List<Gate> gates = gateRepository.findAll();

        flights.stream()
                .filter(flight -> flight.getStatus() == FlightStatus.DELAYED || flight.getStatus() == FlightStatus.WAITING)
                .forEach(flight -> conflicts.add(new ResourceConflict(
                        "航班运行压力",
                        flight.getFlightNo(),
                        "航班处于" + flight.getStatus() + "状态，延误 " + flight.getDelayMinutes() + " 分钟",
                        flight.getDelayMinutes() >= 30 ? "严重" : "预警",
                        recommendForFlight(flight, runways, gates))));

        runways.stream()
                .filter(runway -> runway.getStatus() == ResourceStatus.OCCUPIED && runway.getCurrentFlightId() == null)
                .forEach(runway -> conflicts.add(new ResourceConflict(
                        "跑道占用异常",
                        runway.getRunwayNo(),
                        "跑道被标记为占用，但未关联当前航班",
                        "预警",
                        "核对跑道占用记录，必要时释放跑道")));

        gates.stream()
                .filter(gate -> gate.getStatus() == ResourceStatus.OCCUPIED && gate.getCurrentFlightId() == null)
                .forEach(gate -> conflicts.add(new ResourceConflict(
                        "机位占用异常",
                        gate.getGateNo(),
                        "停机位被标记为占用，但未关联当前航班",
                        "预警",
                        "核对机位占用记录，必要时释放停机位")));

        for (Flight flight : flights) {
            if (flight.getFlightType() == FlightType.ARRIVAL && findCompatibleGate(flight, gates) == null) {
                conflicts.add(new ResourceConflict(
                        "机位匹配不足",
                        flight.getFlightNo(),
                        "未找到空闲且适配 " + flight.getAircraftType() + " 的停机位",
                        "严重",
                        "释放同机型机位或调整到大机型兼容机位"));
            }
            if (flight.getFlightType() == FlightType.DEPARTURE && findCompatibleRunway(flight, runways) == null) {
                conflicts.add(new ResourceConflict(
                        "跑道能力不足",
                        flight.getFlightNo(),
                        "未找到支持起飞且空闲的跑道",
                        "严重",
                        "切换支持起飞跑道或延后放行"));
            }
        }

        return conflicts;
    }

    public List<ResourceRecommendation> getRecommendations() {
        List<Runway> runways = runwayRepository.findAll();
        List<Gate> gates = gateRepository.findAll();
        return flightRepository.findAll().stream()
                .filter(flight -> flight.getStatus() == FlightStatus.SCHEDULED
                        || flight.getStatus() == FlightStatus.WAITING
                        || flight.getStatus() == FlightStatus.DELAYED)
                .sorted(Comparator.comparingInt(Flight::getPriority).reversed()
                        .thenComparing(this::operationTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(12)
                .map(flight -> {
                    Runway runway = findCompatibleRunway(flight, runways);
                    Gate gate = findCompatibleGate(flight, gates);
                    String resource = flight.getFlightType() == FlightType.ARRIVAL
                            ? gate == null ? "暂无可用机位" : gate.getGateNo()
                            : runway == null ? "暂无可用跑道" : runway.getRunwayNo();
                    String reason = flight.getFlightType() == FlightType.ARRIVAL
                            ? "按机型、空闲状态和优先级推荐停机位"
                            : "按起降方向、空闲状态和优先级推荐跑道";
                    return new ResourceRecommendation(flight.getFlightNo(), flight.getFlightType().name(), resource, reason);
                })
                .toList();
    }

    public List<IncidentItem> getIncidents() {
        return simulationRecordRepository.findAllByOrderByEventTimeDescIdDesc().stream()
                .limit(30)
                .map(record -> new IncidentItem(
                        record.getId(),
                        format(record.getEventTime()),
                        severity(record),
                        record.getEventType().name(),
                        record.getDescription(),
                        record.getFlightId() == null ? "-" : String.valueOf(record.getFlightId()),
                        record.getEventType().name().contains("DELAY") ? "处理中" : "已处理",
                        actionFor(record)))
                .toList();
    }

    public List<ScheduleItem> getScheduleItems() {
        List<ScheduleItem> items = new ArrayList<>();
        flightRepository.findAll().stream()
                .sorted(Comparator.comparing(this::operationTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(24)
                .forEach(flight -> items.add(new ScheduleItem(
                        flight.getFlightNo(),
                        flight.getFlightType().name(),
                        "航班计划",
                        format(operationTime(flight)),
                        Math.min(95, 20 + flight.getPriority() * 10 + flight.getDelayMinutes()),
                        flight.getStatus().name())));
        runwayRepository.findAll().forEach(runway -> items.add(new ScheduleItem(
                runway.getRunwayNo(),
                "RUNWAY",
                runway.getSupportType().name(),
                format(runway.getOccupiedEndTime()),
                runway.getStatus() == ResourceStatus.OCCUPIED ? 72 : 18,
                runway.getStatus().name())));
        gateRepository.findAll().forEach(gate -> items.add(new ScheduleItem(
                gate.getGateNo(),
                "GATE",
                gate.getAircraftType().name(),
                format(gate.getOccupiedEndTime()),
                gate.getStatus() == ResourceStatus.OCCUPIED ? 64 : 16,
                gate.getStatus().name())));
        return items;
    }

    public List<ScenarioComparison> getScenarioComparisons() {
        StatisticsSummary summary = statisticsService.getSummary();
        return List.of(
                new ScenarioComparison("日常运行", summary.getCompletionRate(), summary.getDelayRate(), summary.getRunwayOccupancyRate(), "基准方案"),
                new ScenarioComparison("雷雨保障", Math.max(0, summary.getCompletionRate() - 8), summary.getDelayRate() + 12, summary.getRunwayOccupancyRate() + 8, "建议延长时间步并预留备用机位"),
                new ScenarioComparison("大流量保障", Math.max(0, summary.getCompletionRate() - 4), summary.getDelayRate() + 7, Math.min(100, summary.getRunwayOccupancyRate() + 14), "建议启用优先级队列"),
                new ScenarioComparison("跑道关闭", Math.max(0, summary.getCompletionRate() - 15), summary.getDelayRate() + 20, 100.0, "建议切换单跑道容量控制"));
    }

    public List<RoleWorkspace> getRoleWorkspaces() {
        return List.of(
                new RoleWorkspace("管理员", "全部数据、系统配置、用户角色、导入导出", "完整权限"),
                new RoleWorkspace("运控人员", "运行概览、异常处置、场景对比、仿真控制", "运行决策"),
                new RoleWorkspace("航班调度员", "航班调度、延误队列、资源推荐", "航班维护"),
                new RoleWorkspace("资源管理员", "跑道资源、停机位、资源甘特图", "资源维护"),
                new RoleWorkspace("观察者", "运行概览、统计分析、事件日志", "只读查看"));
    }

    public String exportRecordsCsv() {
        StringBuilder builder = new StringBuilder("时间,任务ID,航班ID,事件类型,说明\n");
        for (SimulationRecord record : simulationRecordRepository.findAllByOrderByEventTimeDescIdDesc()) {
            builder.append(csv(format(record.getEventTime()))).append(',')
                    .append(record.getTaskId()).append(',')
                    .append(record.getFlightId() == null ? "" : record.getFlightId()).append(',')
                    .append(record.getEventType()).append(',')
                    .append(csv(record.getDescription())).append('\n');
        }
        return builder.toString();
    }

    public String exportStatisticsCsv() {
        StatisticsSummary summary = statisticsService.getSummary();
        return """
                指标,数值
                航班总数,%d
                起飞航班,%d
                降落航班,%d
                已完成航班,%d
                延误航班,%d
                等待航班,%d
                完成率,%.2f%%
                延误率,%.2f%%
                跑道占用率,%.2f%%
                停机位占用率,%.2f%%
                """.formatted(
                summary.getTotalFlights(),
                summary.getDepartureFlights(),
                summary.getArrivalFlights(),
                summary.getCompletedFlights(),
                summary.getDelayedFlights(),
                summary.getWaitingFlights(),
                summary.getCompletionRate(),
                summary.getDelayRate(),
                summary.getRunwayOccupancyRate(),
                summary.getGateOccupancyRate());
    }

    @Transactional
    public ImportResult importFlights(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ImportResult(0, 1, "请选择 CSV 文件");
        }
        int success = 0;
        int failed = 0;
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] lines = content.split("\\R");
            for (int i = 1; i < lines.length; i++) {
                if (!StringUtils.hasText(lines[i])) {
                    continue;
                }
                String[] parts = lines[i].split(",", -1);
                if (parts.length < 7) {
                    failed++;
                    continue;
                }
                Flight flight = new Flight();
                flight.setFlightNo(parts[0].trim());
                flight.setAirline(parts[1].trim());
                flight.setDepartureAirport(parts[2].trim());
                flight.setArrivalAirport(parts[3].trim());
                flight.setFlightType(FlightType.valueOf(parts[4].trim().toUpperCase(Locale.ROOT)));
                flight.setAircraftType(AircraftType.valueOf(parts[5].trim().toUpperCase(Locale.ROOT)));
                if (flight.getFlightType() == FlightType.DEPARTURE) {
                    flight.setPlannedDepartureTime(LocalDateTime.parse(parts[6].trim()));
                } else {
                    flight.setPlannedArrivalTime(LocalDateTime.parse(parts[6].trim()));
                }
                flight.setStatus(FlightStatus.SCHEDULED);
                flightRepository.save(flight);
                success++;
            }
        } catch (Exception ex) {
            return new ImportResult(success, failed + 1, "导入中断：" + ex.getMessage());
        }
        return new ImportResult(success, failed, "导入完成");
    }

    private Runway findCompatibleRunway(Flight flight, List<Runway> runways) {
        return runways.stream()
                .filter(runway -> runway.getStatus() == ResourceStatus.IDLE)
                .filter(runway -> runway.getSupportType().name().equals("BOTH")
                        || flight.getFlightType() == FlightType.DEPARTURE && runway.getSupportType().name().equals("TAKEOFF_ONLY")
                        || flight.getFlightType() == FlightType.ARRIVAL && runway.getSupportType().name().equals("LANDING_ONLY"))
                .findFirst()
                .orElse(null);
    }

    private Gate findCompatibleGate(Flight flight, List<Gate> gates) {
        return gates.stream()
                .filter(gate -> gate.getStatus() == ResourceStatus.IDLE)
                .filter(gate -> gate.getAircraftType() == flight.getAircraftType() || gate.getAircraftType() == AircraftType.LARGE)
                .findFirst()
                .orElse(null);
    }

    private String recommendForFlight(Flight flight, List<Runway> runways, List<Gate> gates) {
        if (flight.getFlightType() == FlightType.ARRIVAL) {
            Gate gate = findCompatibleGate(flight, gates);
            return gate == null ? "暂无空闲适配机位，建议进入等待队列" : "推荐分配停机位 " + gate.getGateNo();
        }
        Runway runway = findCompatibleRunway(flight, runways);
        return runway == null ? "暂无空闲起飞跑道，建议延后放行" : "推荐使用跑道 " + runway.getRunwayNo();
    }

    private String severity(SimulationRecord record) {
        String type = record.getEventType().name();
        if (type.contains("DELAY") || type.contains("QUEUE")) {
            return "告警";
        }
        if (type.contains("RELEASE") || type.contains("COMPLETE")) {
            return "提示";
        }
        return "通知";
    }

    private String actionFor(SimulationRecord record) {
        String type = record.getEventType().name();
        if (type.contains("DELAY")) {
            return "重排延误队列并检查可用资源";
        }
        if (type.contains("ASSIGN")) {
            return "确认资源占用窗口";
        }
        if (type.contains("RELEASE")) {
            return "同步释放跑道或停机位";
        }
        return "记录归档";
    }

    private LocalDateTime operationTime(Flight flight) {
        return flight.getFlightType() == FlightType.DEPARTURE
                ? flight.getPlannedDepartureTime()
                : flight.getPlannedArrivalTime();
    }

    private String format(LocalDateTime time) {
        return time == null ? "-" : time.format(TIME_FORMATTER);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public record ResourceConflict(String type, String target, String description, String severity, String suggestion) {}

    public record ResourceRecommendation(String flightNo, String flightType, String recommendedResource, String reason) {}

    public record IncidentItem(Long id, String eventTime, String severity, String eventType, String description,
                               String relatedObject, String status, String action) {}

    public record ScheduleItem(String resource, String resourceType, String label, String time, int widthPercent, String status) {}

    public record ScenarioComparison(String scenarioName, double completionRate, double delayRate,
                                     double runwayOccupancyRate, String recommendation) {}

    public record RoleWorkspace(String roleName, String visibleModules, String permissionScope) {}

    public record ImportResult(int successCount, int failedCount, String message) {}
}

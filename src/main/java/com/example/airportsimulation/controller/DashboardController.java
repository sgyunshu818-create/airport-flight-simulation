package com.example.airportsimulation.controller;

import com.example.airportsimulation.service.SimulationService;
import com.example.airportsimulation.service.StatisticsService;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import com.example.airportsimulation.service.FlightService;
import com.example.airportsimulation.service.GateService;
import com.example.airportsimulation.service.RunwayService;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DashboardController {
    private final StatisticsService statisticsService;
    private final SimulationService simulationService;
    private final FlightService flightService;
    private final RunwayService runwayService;
    private final GateService gateService;
    private final SimulationRecordRepository simulationRecordRepository;

    public DashboardController(
            StatisticsService statisticsService,
            SimulationService simulationService,
            FlightService flightService,
            RunwayService runwayService,
            GateService gateService,
            SimulationRecordRepository simulationRecordRepository) {
        this.statisticsService = statisticsService;
        this.simulationService = simulationService;
        this.flightService = flightService;
        this.runwayService = runwayService;
        this.gateService = gateService;
        this.simulationRecordRepository = simulationRecordRepository;
    }

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("summary", statisticsService.getSummary());
        model.addAttribute("flights", flightService.findAll(null, null));
        model.addAttribute("runways", runwayService.findAll());
        model.addAttribute("gates", gateService.findAll());
        model.addAttribute("recentRecords", simulationRecordRepository.findAllByOrderByEventTimeDescIdDesc()
                .stream()
                .limit(4)
                .toList());
        try {
            model.addAttribute("latestTask", simulationService.getLatestTask());
        } catch (NoSuchElementException ignored) {
            model.addAttribute("latestTask", null);
        }
        return "dashboard";
    }
}

package com.example.airportsimulation.controller;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.SimulationTask;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import com.example.airportsimulation.service.FlightService;
import com.example.airportsimulation.service.GateService;
import com.example.airportsimulation.service.RunwayService;
import com.example.airportsimulation.service.SimulationService;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SimulationController {
    private final SimulationService simulationService;
    private final FlightService flightService;
    private final RunwayService runwayService;
    private final GateService gateService;
    private final SimulationRecordRepository simulationRecordRepository;

    public SimulationController(
            SimulationService simulationService,
            FlightService flightService,
            RunwayService runwayService,
            GateService gateService,
            SimulationRecordRepository simulationRecordRepository) {
        this.simulationService = simulationService;
        this.flightService = flightService;
        this.runwayService = runwayService;
        this.gateService = gateService;
        this.simulationRecordRepository = simulationRecordRepository;
    }

    @GetMapping("/simulation")
    public String simulation(Model model) {
        model.addAttribute("taskForm", new SimulationTask());
        try {
            model.addAttribute("latestTask", simulationService.getLatestTask());
        } catch (NoSuchElementException ignored) {
            model.addAttribute("latestTask", null);
        }
        addSimulationState(model);
        return "simulation";
    }

    @PostMapping("/simulation/tasks")
    public String createTask(@ModelAttribute("taskForm") SimulationTask task, Model model) {
        try {
            simulationService.createTask(task);
            return "redirect:/simulation";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("latestTask", null);
            addSimulationState(model);
            return "simulation";
        }
    }

    @PostMapping("/simulation/tasks/{id}/step")
    public String step(@PathVariable Long id) {
        simulationService.step(id);
        return "redirect:/simulation";
    }

    @PostMapping("/simulation/tasks/{id}/run")
    public String run(@PathVariable Long id) {
        simulationService.runAll(id);
        return "redirect:/simulation";
    }

    @PostMapping("/simulation/reset")
    public String reset() {
        simulationService.reset();
        return "redirect:/simulation";
    }

    private void addSimulationState(Model model) {
        List<Flight> flights = flightService.findAll(null, null);
        model.addAttribute("flights", flights);
        model.addAttribute("runways", runwayService.findAll());
        model.addAttribute("gates", gateService.findAll());
        model.addAttribute("queueFlights", flights.stream()
                .filter(flight -> flight.getStatus() == FlightStatus.WAITING
                        || flight.getStatus() == FlightStatus.DELAYED)
                .toList());
        model.addAttribute("recentRecords", simulationRecordRepository.findAllByOrderByEventTimeDescIdDesc()
                .stream()
                .limit(8)
                .toList());
    }
}

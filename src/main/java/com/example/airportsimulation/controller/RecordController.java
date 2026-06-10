package com.example.airportsimulation.controller;

import com.example.airportsimulation.enums.SimulationEventType;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecordController {
    private final SimulationRecordRepository simulationRecordRepository;

    public RecordController(SimulationRecordRepository simulationRecordRepository) {
        this.simulationRecordRepository = simulationRecordRepository;
    }

    @GetMapping("/records")
    public String records(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) SimulationEventType eventType,
            Model model) {
        model.addAttribute("records", simulationRecordRepository.findByFilters(taskId, flightId, eventType));
        model.addAttribute("taskId", taskId);
        model.addAttribute("flightId", flightId);
        model.addAttribute("selectedEventType", eventType);
        model.addAttribute("eventTypes", SimulationEventType.values());
        return "records";
    }
}

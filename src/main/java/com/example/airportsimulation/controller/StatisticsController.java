package com.example.airportsimulation.controller;

import com.example.airportsimulation.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("summary", statisticsService.getSummary());
        return "statistics";
    }
}

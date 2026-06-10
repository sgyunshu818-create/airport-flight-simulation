package com.example.airportsimulation.controller;

import com.example.airportsimulation.service.TestScenarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TestScenarioController {
    private final TestScenarioService testScenarioService;

    public TestScenarioController(TestScenarioService testScenarioService) {
        this.testScenarioService = testScenarioService;
    }

    @PostMapping("/simulation/test-scenarios/{scenarioKey}")
    public String loadScenario(@PathVariable String scenarioKey, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("scenarioMessage", testScenarioService.load(scenarioKey));
        return "redirect:/simulation";
    }
}

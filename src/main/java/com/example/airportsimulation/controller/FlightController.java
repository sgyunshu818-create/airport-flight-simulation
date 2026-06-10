package com.example.airportsimulation.controller;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.service.FlightService;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/flights")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) FlightStatus status,
            @RequestParam(required = false) FlightType flightType,
            @RequestParam(required = false) String airline,
            @RequestParam(required = false) String error,
            Model model) {
        model.addAttribute("flights", flightService.findAll(keyword, status, flightType, airline));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedFlightType", flightType);
        model.addAttribute("airline", airline);
        model.addAttribute("error", error);
        addFlightEnums(model);
        return "flights";
    }

    @GetMapping("/flights/new")
    public String createForm(Model model) {
        model.addAttribute("flight", new Flight());
        model.addAttribute("formTitle", "新增航班");
        addFlightEnums(model);
        return "flight-form";
    }

    @PostMapping("/flights")
    public String create(@ModelAttribute Flight flight, Model model) {
        return saveFlight(flight, model);
    }

    @GetMapping("/flights/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("flight", flightService.findById(id));
        model.addAttribute("formTitle", "编辑航班");
        addFlightEnums(model);
        return "flight-form";
    }

    @PostMapping("/flights/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Flight flight, Model model) {
        flight.setId(id);
        return saveFlight(flight, model);
    }

    @PostMapping("/flights/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            flightService.deleteById(id);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }
        return "redirect:/flights";
    }

    private String saveFlight(Flight flight, Model model) {
        try {
            flightService.save(flight);
            return "redirect:/flights";
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            model.addAttribute("flight", flight);
            model.addAttribute("formTitle", flight.getId() == null ? "新增航班" : "编辑航班");
            model.addAttribute("error", ex.getMessage());
            addFlightEnums(model);
            return "flight-form";
        }
    }

    private void addFlightEnums(Model model) {
        model.addAttribute("flightTypes", FlightType.values());
        model.addAttribute("aircraftTypes", AircraftType.values());
        model.addAttribute("statuses", FlightStatus.values());
    }
}



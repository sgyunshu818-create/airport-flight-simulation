package com.example.airportsimulation.controller;

import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.service.GateService;
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
public class GateController {
    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @GetMapping("/gates")
    public String list(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("gates", gateService.findAll());
        model.addAttribute("error", error);
        return "gates";
    }

    @GetMapping("/gates/new")
    public String createForm(Model model) {
        model.addAttribute("gate", new Gate());
        model.addAttribute("formTitle", "新增停机位");
        addGateEnums(model);
        return "gate-form";
    }

    @PostMapping("/gates")
    public String create(@ModelAttribute Gate gate, Model model) {
        return saveGate(gate, model);
    }

    @GetMapping("/gates/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("gate", gateService.findById(id));
        model.addAttribute("formTitle", "编辑停机位");
        addGateEnums(model);
        return "gate-form";
    }

    @PostMapping("/gates/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Gate gate, Model model) {
        gate.setId(id);
        return saveGate(gate, model);
    }

    @PostMapping("/gates/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            gateService.deleteById(id);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }
        return "redirect:/gates";
    }

    private String saveGate(Gate gate, Model model) {
        try {
            gateService.save(gate);
            return "redirect:/gates";
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            model.addAttribute("gate", gate);
            model.addAttribute("formTitle", gate.getId() == null ? "新增停机位" : "编辑停机位");
            model.addAttribute("error", ex.getMessage());
            addGateEnums(model);
            return "gate-form";
        }
    }

    private void addGateEnums(Model model) {
        model.addAttribute("statuses", ResourceStatus.values());
        model.addAttribute("aircraftTypes", AircraftType.values());
    }
}

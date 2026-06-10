package com.example.airportsimulation.controller;

import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
import com.example.airportsimulation.service.RunwayService;
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
public class RunwayController {
    private final RunwayService runwayService;

    public RunwayController(RunwayService runwayService) {
        this.runwayService = runwayService;
    }

    @GetMapping("/runways")
    public String list(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("runways", runwayService.findAll());
        model.addAttribute("error", error);
        return "runways";
    }

    @GetMapping("/runways/new")
    public String createForm(Model model) {
        model.addAttribute("runway", new Runway());
        model.addAttribute("formTitle", "新增跑道");
        addRunwayEnums(model);
        return "runway-form";
    }

    @PostMapping("/runways")
    public String create(@ModelAttribute Runway runway, Model model) {
        return saveRunway(runway, model);
    }

    @GetMapping("/runways/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("runway", runwayService.findById(id));
        model.addAttribute("formTitle", "编辑跑道");
        addRunwayEnums(model);
        return "runway-form";
    }

    @PostMapping("/runways/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Runway runway, Model model) {
        runway.setId(id);
        return saveRunway(runway, model);
    }

    @PostMapping("/runways/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            runwayService.deleteById(id);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }
        return "redirect:/runways";
    }

    private String saveRunway(Runway runway, Model model) {
        try {
            runwayService.save(runway);
            return "redirect:/runways";
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            model.addAttribute("runway", runway);
            model.addAttribute("formTitle", runway.getId() == null ? "新增跑道" : "编辑跑道");
            model.addAttribute("error", ex.getMessage());
            addRunwayEnums(model);
            return "runway-form";
        }
    }

    private void addRunwayEnums(Model model) {
        model.addAttribute("statuses", ResourceStatus.values());
        model.addAttribute("supportTypes", RunwaySupportType.values());
    }
}

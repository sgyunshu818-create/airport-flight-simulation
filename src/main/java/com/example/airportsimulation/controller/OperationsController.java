package com.example.airportsimulation.controller;

import com.example.airportsimulation.service.OperationsInsightService;
import com.example.airportsimulation.service.OperationsInsightService.ImportResult;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OperationsController {
    private final OperationsInsightService operationsInsightService;

    public OperationsController(OperationsInsightService operationsInsightService) {
        this.operationsInsightService = operationsInsightService;
    }

    @GetMapping("/operations/conflicts")
    public String conflicts(Model model) {
        model.addAttribute("conflicts", operationsInsightService.findConflicts());
        model.addAttribute("recommendations", operationsInsightService.getRecommendations());
        return "operations-conflicts";
    }

    @GetMapping("/operations/incidents")
    public String incidents(Model model) {
        model.addAttribute("incidents", operationsInsightService.getIncidents());
        return "operations-incidents";
    }

    @GetMapping("/operations/schedule")
    public String schedule(Model model) {
        model.addAttribute("items", operationsInsightService.getScheduleItems());
        return "operations-schedule";
    }

    @GetMapping("/operations/data")
    public String data() {
        return "operations-data";
    }

    @PostMapping("/operations/import-flights")
    public String importFlights(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        ImportResult result = operationsInsightService.importFlights(file);
        redirectAttributes.addFlashAttribute("importMessage", result.message());
        redirectAttributes.addFlashAttribute("successCount", result.successCount());
        redirectAttributes.addFlashAttribute("failedCount", result.failedCount());
        return "redirect:/operations/data";
    }

    @GetMapping("/operations/export/records.csv")
    public ResponseEntity<byte[]> exportRecords() {
        return csv("simulation-records.csv", operationsInsightService.exportRecordsCsv());
    }

    @GetMapping("/operations/export/statistics.csv")
    public ResponseEntity<byte[]> exportStatistics() {
        return csv("statistics-summary.csv", operationsInsightService.exportStatisticsCsv());
    }

    @GetMapping("/operations/scenarios")
    public String scenarios(Model model) {
        model.addAttribute("scenarios", operationsInsightService.getScenarioComparisons());
        return "operations-scenarios";
    }

    @GetMapping("/operations/roles")
    public String roles(Model model) {
        model.addAttribute("roles", operationsInsightService.getRoleWorkspaces());
        return "operations-roles";
    }

    private ResponseEntity<byte[]> csv(String filename, String content) {
        byte[] bytes = withUtf8Bom(content);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private byte[] withUtf8Bom(String content) {
        byte[] csvBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = Arrays.copyOf(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, csvBytes.length + 3);
        System.arraycopy(csvBytes, 0, bytes, 3, csvBytes.length);
        return bytes;
    }
}

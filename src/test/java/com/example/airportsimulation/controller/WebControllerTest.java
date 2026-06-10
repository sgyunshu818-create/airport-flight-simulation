package com.example.airportsimulation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.SimulationTask;
import com.example.airportsimulation.repository.SimulationRecordRepository;
import com.example.airportsimulation.service.FlightService;
import com.example.airportsimulation.service.GateService;
import com.example.airportsimulation.service.RunwayService;
import com.example.airportsimulation.service.SimulationService;
import com.example.airportsimulation.service.StatisticsService;
import com.example.airportsimulation.service.StatisticsSummary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({
        DashboardController.class,
        StatisticsController.class,
        FlightController.class,
        RunwayController.class,
        GateController.class,
        SimulationController.class,
        RecordController.class
})
class WebControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @MockBean
    private RunwayService runwayService;

    @MockBean
    private GateService gateService;

    @MockBean
    private SimulationService simulationService;

    @MockBean
    private StatisticsService statisticsService;

    @MockBean
    private SimulationRecordRepository simulationRecordRepository;

    @BeforeEach
    void setUp() {
        when(statisticsService.getSummary()).thenReturn(new StatisticsSummary(
                8, 4, 4, 3, 2, 5.0, 10, 1, 2, 1));
        when(flightService.findAll(null, null)).thenReturn(List.of());
        when(runwayService.findAll()).thenReturn(List.of());
        when(gateService.findAll()).thenReturn(List.of());
        when(simulationRecordRepository.findAllByOrderByEventTimeDescIdDesc()).thenReturn(List.of());
    }

    @Test
    void loginPageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void loginSubmitRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void dashboardShowsSummary() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("summary"));
    }

    @Test
    void statisticsPageShowsSummary() throws Exception {
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics"))
                .andExpect(model().attributeExists("summary"));
    }

    @Test
    void flightListAndFormPagesLoad() throws Exception {
        mockMvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(view().name("flights"))
                .andExpect(model().attributeExists("flights", "statuses"));

        mockMvc.perform(get("/flights/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("flight-form"))
                .andExpect(model().attributeExists("flight", "flightTypes", "aircraftTypes", "statuses"));
    }

    @Test
    void flightCreateRedirectsToFlightList() throws Exception {
        when(flightService.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/flights")
                        .param("flightNo", "CA1001")
                        .param("airline", "Air China")
                        .param("departureAirport", "PEK")
                        .param("arrivalAirport", "SHA")
                        .param("flightType", "DEPARTURE")
                        .param("aircraftType", "MEDIUM")
                        .param("plannedDepartureTime", "2026-05-29T09:00")
                        .param("status", "SCHEDULED")
                        .param("delayMinutes", "0")
                        .param("priority", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/flights"));
    }

    @Test
    void runwayAndGateListPagesLoad() throws Exception {
        mockMvc.perform(get("/runways"))
                .andExpect(status().isOk())
                .andExpect(view().name("runways"))
                .andExpect(model().attributeExists("runways"));

        mockMvc.perform(get("/gates"))
                .andExpect(status().isOk())
                .andExpect(view().name("gates"))
                .andExpect(model().attributeExists("gates"));
    }

    @Test
    void simulationPageLoadsWithoutExistingTask() throws Exception {
        when(simulationService.getLatestTask()).thenThrow(new NoSuchElementException("No task"));

        mockMvc.perform(get("/simulation"))
                .andExpect(status().isOk())
                .andExpect(view().name("simulation"))
                .andExpect(model().attributeExists("taskForm"));
    }

    @Test
    void simulationActionsRedirectToSimulationPage() throws Exception {
        SimulationTask task = new SimulationTask();
        task.setId(1L);
        task.setTaskName("Demo");
        task.setStartTime(LocalDateTime.of(2026, 5, 29, 9, 0));
        task.setEndTime(LocalDateTime.of(2026, 5, 29, 10, 0));
        task.setCurrentTime(task.getStartTime());
        task.setTimeStepMinutes(10);
        when(simulationService.createTask(any(SimulationTask.class))).thenReturn(task);
        when(simulationService.step(1L)).thenReturn(task);

        mockMvc.perform(post("/simulation/tasks")
                        .param("taskName", "Demo")
                        .param("startTime", "2026-05-29T09:00")
                        .param("endTime", "2026-05-29T10:00")
                        .param("timeStepMinutes", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/simulation"));

        mockMvc.perform(post("/simulation/tasks/1/step"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/simulation"));
    }

    @Test
    void recordsPageLoads() throws Exception {
        mockMvc.perform(get("/records"))
                .andExpect(status().isOk())
                .andExpect(view().name("records"))
                .andExpect(model().attributeExists("records"));
    }
}


package com.example.airportsimulation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.repository.FlightRepository;
import com.example.airportsimulation.repository.GateRepository;
import com.example.airportsimulation.repository.RunwayRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(StatisticsService.class)
@ActiveProfiles("test")
class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private RunwayRepository runwayRepository;

    @Autowired
    private GateRepository gateRepository;

    @Test
    void summaryCalculatesFlightCountsDelaysAndIdleResources() {
        flightRepository.save(flight("MU5101", FlightType.DEPARTURE, FlightStatus.COMPLETED, 0));
        flightRepository.save(flight("CA1202", FlightType.ARRIVAL, FlightStatus.DELAYED, 20));
        flightRepository.save(flight("CZ3301", FlightType.DEPARTURE, FlightStatus.WAITING, 10));
        runwayRepository.save(runway("R01", ResourceStatus.IDLE));
        runwayRepository.save(runway("R02", ResourceStatus.OCCUPIED));
        gateRepository.save(gate("G01", ResourceStatus.IDLE));
        gateRepository.save(gate("G02", ResourceStatus.OCCUPIED));

        StatisticsSummary summary = statisticsService.getSummary();

        assertThat(summary.getTotalFlights()).isEqualTo(3);
        assertThat(summary.getDepartureFlights()).isEqualTo(2);
        assertThat(summary.getArrivalFlights()).isEqualTo(1);
        assertThat(summary.getCompletedFlights()).isEqualTo(1);
        assertThat(summary.getDelayedFlights()).isEqualTo(1);
        assertThat(summary.getWaitingFlights()).isEqualTo(1);
        assertThat(summary.getAverageDelayMinutes()).isEqualTo(10.0);
        assertThat(summary.getMaxDelayMinutes()).isEqualTo(20);
        assertThat(summary.getIdleRunways()).isEqualTo(1);
        assertThat(summary.getIdleGates()).isEqualTo(1);
        assertThat(summary.getCompletionRate()).isEqualTo(33.33);
        assertThat(summary.getDelayRate()).isEqualTo(33.33);
        assertThat(summary.getRunwayOccupancyRate()).isEqualTo(50.0);
        assertThat(summary.getGateOccupancyRate()).isEqualTo(50.0);
    }

    private Flight flight(String flightNo, FlightType type, FlightStatus status, int delayMinutes) {
        Flight flight = new Flight();
        flight.setFlightNo(flightNo);
        flight.setFlightType(type);
        flight.setAircraftType(AircraftType.MEDIUM);
        flight.setStatus(status);
        flight.setDelayMinutes(delayMinutes);
        if (type == FlightType.DEPARTURE) {
            flight.setPlannedDepartureTime(LocalDateTime.of(2026, 6, 1, 8, 0));
        } else {
            flight.setPlannedArrivalTime(LocalDateTime.of(2026, 6, 1, 8, 0));
        }
        return flight;
    }

    private Runway runway(String runwayNo, ResourceStatus status) {
        Runway runway = new Runway();
        runway.setRunwayNo(runwayNo);
        runway.setStatus(status);
        return runway;
    }

    private Gate gate(String gateNo, ResourceStatus status) {
        Gate gate = new Gate();
        gate.setGateNo(gateNo);
        gate.setAircraftType(AircraftType.MEDIUM);
        gate.setStatus(status);
        return gate;
    }
}



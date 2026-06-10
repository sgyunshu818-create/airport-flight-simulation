package com.example.airportsimulation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.repository.FlightRepository;
import com.example.airportsimulation.repository.GateRepository;
import com.example.airportsimulation.repository.RunwayRepository;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FlightService.class, RunwayService.class, GateService.class})
@ActiveProfiles("test")
class BasicCrudServiceTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private RunwayService runwayService;

    @Autowired
    private GateService gateService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private RunwayRepository runwayRepository;

    @Autowired
    private GateRepository gateRepository;

    @Test
    void flightServiceSavesValidFlightWithDefaultsAndSupportsQueries() {
        Flight flight = new Flight();
        flight.setFlightNo("MU5101");
        flight.setAirline("China Eastern");
        flight.setDepartureAirport("SHA");
        flight.setArrivalAirport("PEK");
        flight.setFlightType(FlightType.DEPARTURE);
        flight.setAircraftType(AircraftType.MEDIUM);
        flight.setPlannedDepartureTime(LocalDateTime.of(2026, 6, 1, 8, 0));

        Flight saved = flightService.save(flight);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(saved.getDelayMinutes()).isZero();
        assertThat(saved.getPriority()).isZero();
        assertThat(flightService.findById(saved.getId()).getFlightNo()).isEqualTo("MU5101");
        assertThat(flightService.findAll("mu51", null, null, null)).extracting(Flight::getFlightNo).containsExactly("MU5101");
        assertThat(flightService.findAll(null, FlightStatus.SCHEDULED, null, null)).extracting(Flight::getFlightNo).contains("MU5101");
    }

    @Test
    void flightServiceRejectsMissingRequiredFieldsAndMissingPlannedTime() {
        assertThatThrownBy(() -> flightService.save(new Flight()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("航班号");

        Flight arrival = new Flight();
        arrival.setFlightNo("CA1202");
        arrival.setFlightType(FlightType.ARRIVAL);
        arrival.setAircraftType(AircraftType.LARGE);

        assertThatThrownBy(() -> flightService.save(arrival))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("计划降落时间");
    }

    @Test
    void runwayServiceSavesDefaultsFindsAndDeletesRunways() {
        Runway runway = new Runway();
        runway.setRunwayNo("R01");

        Runway saved = runwayService.save(runway);

        assertThat(saved.getStatus()).isEqualTo(ResourceStatus.IDLE);
        assertThat(saved.getSupportType()).isEqualTo(RunwaySupportType.BOTH);
        assertThat(runwayService.findAll()).extracting(Runway::getRunwayNo).contains("R01");

        runwayService.deleteById(saved.getId());

        assertThatThrownBy(() -> runwayService.findById(saved.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void runwayServiceRejectsMissingRunwayNo() {
        assertThatThrownBy(() -> runwayService.save(new Runway()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("跑道编号");
    }

    @Test
    void gateServiceSavesDefaultsFindsAndDeletesGates() {
        Gate gate = new Gate();
        gate.setGateNo("G01");
        gate.setAircraftType(AircraftType.SMALL);

        Gate saved = gateService.save(gate);

        assertThat(saved.getStatus()).isEqualTo(ResourceStatus.IDLE);
        assertThat(gateService.findAll()).extracting(Gate::getGateNo).contains("G01");

        gateService.deleteById(saved.getId());

        assertThatThrownBy(() -> gateService.findById(saved.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void gateServiceRejectsMissingRequiredFields() {
        assertThatThrownBy(() -> gateService.save(new Gate()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("停机位编号");

        Gate gate = new Gate();
        gate.setGateNo("G02");

        assertThatThrownBy(() -> gateService.save(gate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("适配机型");
    }
}




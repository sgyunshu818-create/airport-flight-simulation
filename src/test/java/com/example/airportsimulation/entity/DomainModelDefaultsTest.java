package com.example.airportsimulation.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
import com.example.airportsimulation.enums.SimulationTaskStatus;
import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DomainModelDefaultsTest {

    @Test
    void flightUsesScheduledDefaults() {
        Flight flight = new Flight();

        assertThat(flight.getStatus()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(flight.getDelayMinutes()).isZero();
        assertThat(flight.getPriority()).isZero();
    }

    @Test
    void runwayUsesIdleDefaults() {
        Runway runway = new Runway();

        assertThat(runway.getStatus()).isEqualTo(ResourceStatus.IDLE);
        assertThat(runway.getSupportType()).isEqualTo(RunwaySupportType.BOTH);
    }

    @Test
    void gateUsesIdleDefault() {
        Gate gate = new Gate();

        assertThat(gate.getStatus()).isEqualTo(ResourceStatus.IDLE);
    }

    @Test
    void simulationTaskStartsAtStartTimeWhenMissingCurrentTime() {
        SimulationTask task = new SimulationTask();
        LocalDateTime startTime = LocalDateTime.of(2026, 6, 1, 8, 0);
        task.setStartTime(startTime);

        task.prepareForCreate();

        assertThat(task.getStatus()).isEqualTo(SimulationTaskStatus.CREATED);
        assertThat(task.getCurrentTime()).isEqualTo(startTime);
    }

    @Test
    void simulationTaskCurrentTimeColumnIsQuotedForMySql() throws Exception {
        Field currentTime = SimulationTask.class.getDeclaredField("currentTime");

        assertThat(currentTime.getAnnotation(Column.class).name()).isEqualTo("`current_time`");
    }
}

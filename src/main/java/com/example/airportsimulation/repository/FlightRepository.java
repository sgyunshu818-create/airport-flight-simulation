package com.example.airportsimulation.repository;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.enums.FlightStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByFlightNoContainingIgnoreCase(String flightNo);

    List<Flight> findByStatus(FlightStatus status);
}

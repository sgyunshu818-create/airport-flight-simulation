package com.example.airportsimulation.service;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.repository.FlightRepository;
import com.example.airportsimulation.repository.GateRepository;
import com.example.airportsimulation.repository.RunwayRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatisticsService {
    private final FlightRepository flightRepository;
    private final RunwayRepository runwayRepository;
    private final GateRepository gateRepository;

    public StatisticsService(
            FlightRepository flightRepository,
            RunwayRepository runwayRepository,
            GateRepository gateRepository) {
        this.flightRepository = flightRepository;
        this.runwayRepository = runwayRepository;
        this.gateRepository = gateRepository;
    }

    public StatisticsSummary getSummary() {
        List<Flight> flights = flightRepository.findAll();
        List<Runway> runways = runwayRepository.findAll();
        List<Gate> gates = gateRepository.findAll();

        long totalFlights = flights.size();
        long departureFlights = flights.stream().filter(flight -> flight.getFlightType() == FlightType.DEPARTURE).count();
        long arrivalFlights = flights.stream().filter(flight -> flight.getFlightType() == FlightType.ARRIVAL).count();
        long completedFlights = flights.stream().filter(flight -> flight.getStatus() == FlightStatus.COMPLETED).count();
        long delayedFlights = flights.stream().filter(flight -> flight.getStatus() == FlightStatus.DELAYED).count();
        long waitingFlights = flights.stream().filter(flight -> flight.getStatus() == FlightStatus.WAITING).count();
        double averageDelayMinutes = flights.stream()
                .mapToInt(Flight::getDelayMinutes)
                .average()
                .orElse(0.0);
        int maxDelayMinutes = flights.stream()
                .mapToInt(Flight::getDelayMinutes)
                .max()
                .orElse(0);
        long idleRunways = runways.stream().filter(runway -> runway.getStatus() == ResourceStatus.IDLE).count();
        long idleGates = gates.stream().filter(gate -> gate.getStatus() == ResourceStatus.IDLE).count();
        long occupiedRunways = runways.stream().filter(runway -> runway.getStatus() == ResourceStatus.OCCUPIED).count();
        long occupiedGates = gates.stream().filter(gate -> gate.getStatus() == ResourceStatus.OCCUPIED).count();

        return new StatisticsSummary(
                totalFlights,
                departureFlights,
                arrivalFlights,
                completedFlights,
                delayedFlights,
                round(averageDelayMinutes),
                maxDelayMinutes,
                idleRunways,
                idleGates,
                waitingFlights,
                percentage(completedFlights, totalFlights),
                percentage(delayedFlights, totalFlights),
                percentage(occupiedRunways, runways.size()),
                percentage(occupiedGates, gates.size()));
    }

    private double percentage(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return round(part * 100.0 / total);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

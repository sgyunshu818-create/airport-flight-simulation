package com.example.airportsimulation.repository;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByFlightNoContainingIgnoreCase(String flightNo);

    List<Flight> findByStatus(FlightStatus status);

    @Query("""
            select f from Flight f
            where (:keyword is null or lower(f.flightNo) like lower(concat('%', :keyword, '%')))
              and (:status is null or f.status = :status)
              and (:flightType is null or f.flightType = :flightType)
              and (:airline is null or lower(f.airline) like lower(concat('%', :airline, '%')))
            order by f.plannedDepartureTime asc, f.plannedArrivalTime asc, f.flightNo asc
            """)
    List<Flight> findByFilters(
            @Param("keyword") String keyword,
            @Param("status") FlightStatus status,
            @Param("flightType") FlightType flightType,
            @Param("airline") String airline);
}

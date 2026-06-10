package com.example.airportsimulation.repository;

import com.example.airportsimulation.entity.SimulationRecord;
import com.example.airportsimulation.enums.SimulationEventType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationRecordRepository extends JpaRepository<SimulationRecord, Long> {
    List<SimulationRecord> findByTaskIdOrderByEventTimeAscIdAsc(Long taskId);

    List<SimulationRecord> findAllByOrderByEventTimeDescIdDesc();

    @Query("""
            select r from SimulationRecord r
            where (:taskId is null or r.taskId = :taskId)
              and (:flightId is null or r.flightId = :flightId)
              and (:eventType is null or r.eventType = :eventType)
            order by r.eventTime desc, r.id desc
            """)
    List<SimulationRecord> findByFilters(
            @Param("taskId") Long taskId,
            @Param("flightId") Long flightId,
            @Param("eventType") SimulationEventType eventType);
}

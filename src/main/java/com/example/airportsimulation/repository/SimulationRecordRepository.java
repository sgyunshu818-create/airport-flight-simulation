package com.example.airportsimulation.repository;

import com.example.airportsimulation.entity.SimulationRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationRecordRepository extends JpaRepository<SimulationRecord, Long> {
    List<SimulationRecord> findByTaskIdOrderByEventTimeAscIdAsc(Long taskId);

    List<SimulationRecord> findAllByOrderByEventTimeDescIdDesc();
}

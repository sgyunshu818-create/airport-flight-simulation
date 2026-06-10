package com.example.airportsimulation.repository;

import com.example.airportsimulation.entity.SimulationTask;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationTaskRepository extends JpaRepository<SimulationTask, Long> {
    Optional<SimulationTask> findTopByOrderByCreatedTimeDescIdDesc();
}


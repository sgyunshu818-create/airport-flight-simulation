package com.example.airportsimulation.repository;

import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.enums.ResourceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunwayRepository extends JpaRepository<Runway, Long> {
    Optional<Runway> findByRunwayNo(String runwayNo);

    List<Runway> findByStatus(ResourceStatus status);
}

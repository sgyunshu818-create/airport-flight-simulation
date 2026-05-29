package com.example.airportsimulation.repository;

import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.enums.ResourceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GateRepository extends JpaRepository<Gate, Long> {
    Optional<Gate> findByGateNo(String gateNo);

    List<Gate> findByStatus(ResourceStatus status);
}

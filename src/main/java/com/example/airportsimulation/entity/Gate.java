package com.example.airportsimulation.entity;

import com.example.airportsimulation.enums.AircraftType;
import com.example.airportsimulation.enums.ResourceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate")
public class Gate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gate_no", nullable = false, unique = true)
    private String gateNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status = ResourceStatus.IDLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "aircraft_type", nullable = false)
    private AircraftType aircraftType;

    @Column(name = "current_flight_id")
    private Long currentFlightId;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdTime = now;
        updatedTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGateNo() {
        return gateNo;
    }

    public void setGateNo(String gateNo) {
        this.gateNo = gateNo;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public AircraftType getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(AircraftType aircraftType) {
        this.aircraftType = aircraftType;
    }

    public Long getCurrentFlightId() {
        return currentFlightId;
    }

    public void setCurrentFlightId(Long currentFlightId) {
        this.currentFlightId = currentFlightId;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}

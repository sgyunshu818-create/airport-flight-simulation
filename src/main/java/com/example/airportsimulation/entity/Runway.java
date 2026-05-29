package com.example.airportsimulation.entity;

import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
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
@Table(name = "runway")
public class Runway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "runway_no", nullable = false, unique = true)
    private String runwayNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status = ResourceStatus.IDLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_type", nullable = false)
    private RunwaySupportType supportType = RunwaySupportType.BOTH;

    @Column(name = "current_flight_id")
    private Long currentFlightId;

    @Column(name = "occupied_start_time")
    private LocalDateTime occupiedStartTime;

    @Column(name = "occupied_end_time")
    private LocalDateTime occupiedEndTime;

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

    public String getRunwayNo() {
        return runwayNo;
    }

    public void setRunwayNo(String runwayNo) {
        this.runwayNo = runwayNo;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public RunwaySupportType getSupportType() {
        return supportType;
    }

    public void setSupportType(RunwaySupportType supportType) {
        this.supportType = supportType;
    }

    public Long getCurrentFlightId() {
        return currentFlightId;
    }

    public void setCurrentFlightId(Long currentFlightId) {
        this.currentFlightId = currentFlightId;
    }

    public LocalDateTime getOccupiedStartTime() {
        return occupiedStartTime;
    }

    public void setOccupiedStartTime(LocalDateTime occupiedStartTime) {
        this.occupiedStartTime = occupiedStartTime;
    }

    public LocalDateTime getOccupiedEndTime() {
        return occupiedEndTime;
    }

    public void setOccupiedEndTime(LocalDateTime occupiedEndTime) {
        this.occupiedEndTime = occupiedEndTime;
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

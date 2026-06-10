package com.example.airportsimulation.service;

import com.example.airportsimulation.entity.Flight;
import com.example.airportsimulation.enums.FlightStatus;
import com.example.airportsimulation.enums.FlightType;
import com.example.airportsimulation.repository.FlightRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class FlightService {
    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public List<Flight> findAll(String keyword, FlightStatus status) {
        return findAll(keyword, status, null, null);
    }

    @Transactional(readOnly = true)
    public List<Flight> findAll(String keyword, FlightStatus status, FlightType flightType, String airline) {
        return flightRepository.findByFilters(
                clean(keyword),
                status,
                flightType,
                clean(airline));
    }

    @Transactional(readOnly = true)
    public Flight findById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("未找到航班：" + id));
    }

    public Flight save(Flight flight) {
        validate(flight);
        if (flight.getStatus() == null) {
            flight.setStatus(FlightStatus.SCHEDULED);
        }
        if (flight.getDelayMinutes() < 0) {
            flight.setDelayMinutes(0);
        }
        if (flight.getPriority() < 0) {
            flight.setPriority(0);
        }
        return flightRepository.save(flight);
    }

    public void deleteById(Long id) {
        Flight flight = findById(id);
        if (flight.getStatus() == FlightStatus.WAITING
                || flight.getStatus() == FlightStatus.DELAYED
                || flight.getStatus() == FlightStatus.RUNNING
                || flight.getStatus() == FlightStatus.COMPLETED) {
            throw new IllegalStateException("不能删除已进入仿真流程的航班，请先重置仿真或保留记录。 ");
        }
        flightRepository.deleteById(id);
    }

    private void validate(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("航班信息不能为空");
        }
        if (!StringUtils.hasText(flight.getFlightNo())) {
            throw new IllegalArgumentException("航班号不能为空");
        }
        if (flight.getFlightType() == null) {
            throw new IllegalArgumentException("航班类型不能为空");
        }
        if (flight.getAircraftType() == null) {
            throw new IllegalArgumentException("机型不能为空");
        }
        if (flight.getFlightType() == FlightType.DEPARTURE && flight.getPlannedDepartureTime() == null) {
            throw new IllegalArgumentException("起飞航班必须填写计划起飞时间");
        }
        if (flight.getFlightType() == FlightType.ARRIVAL && flight.getPlannedArrivalTime() == null) {
            throw new IllegalArgumentException("到达航班必须填写计划降落时间");
        }
    }

    private String clean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

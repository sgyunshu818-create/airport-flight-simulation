package com.example.airportsimulation.service;

import com.example.airportsimulation.entity.Gate;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.repository.GateRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class GateService {
    private final GateRepository gateRepository;

    public GateService(GateRepository gateRepository) {
        this.gateRepository = gateRepository;
    }

    @Transactional(readOnly = true)
    public List<Gate> findAll() {
        return gateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Gate findById(Long id) {
        return gateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("未找到停机位：" + id));
    }

    public Gate save(Gate gate) {
        validate(gate);
        if (gate.getStatus() == null) {
            gate.setStatus(ResourceStatus.IDLE);
        }
        return gateRepository.save(gate);
    }

    public void deleteById(Long id) {
        Gate gate = findById(id);
        if (gate.getStatus() == ResourceStatus.OCCUPIED || gate.getCurrentFlightId() != null) {
            throw new IllegalStateException("停机位正在占用，不能删除");
        }
        gateRepository.deleteById(id);
    }

    private void validate(Gate gate) {
        if (gate == null) {
            throw new IllegalArgumentException("停机位信息不能为空");
        }
        if (!StringUtils.hasText(gate.getGateNo())) {
            throw new IllegalArgumentException("停机位编号不能为空");
        }
        if (gate.getAircraftType() == null) {
            throw new IllegalArgumentException("停机位适配机型不能为空");
        }
    }
}

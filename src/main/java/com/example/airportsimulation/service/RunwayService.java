package com.example.airportsimulation.service;

import com.example.airportsimulation.entity.Runway;
import com.example.airportsimulation.enums.ResourceStatus;
import com.example.airportsimulation.enums.RunwaySupportType;
import com.example.airportsimulation.repository.RunwayRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class RunwayService {
    private final RunwayRepository runwayRepository;

    public RunwayService(RunwayRepository runwayRepository) {
        this.runwayRepository = runwayRepository;
    }

    @Transactional(readOnly = true)
    public List<Runway> findAll() {
        return runwayRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Runway findById(Long id) {
        return runwayRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("未找到跑道：" + id));
    }

    public Runway save(Runway runway) {
        validate(runway);
        if (runway.getStatus() == null) {
            runway.setStatus(ResourceStatus.IDLE);
        }
        if (runway.getSupportType() == null) {
            runway.setSupportType(RunwaySupportType.BOTH);
        }
        return runwayRepository.save(runway);
    }

    public void deleteById(Long id) {
        Runway runway = findById(id);
        if (runway.getStatus() == ResourceStatus.OCCUPIED || runway.getCurrentFlightId() != null) {
            throw new IllegalStateException("跑道正在占用，不能删除");
        }
        runwayRepository.deleteById(id);
    }

    private void validate(Runway runway) {
        if (runway == null) {
            throw new IllegalArgumentException("跑道信息不能为空");
        }
        if (!StringUtils.hasText(runway.getRunwayNo())) {
            throw new IllegalArgumentException("跑道编号不能为空");
        }
    }
}

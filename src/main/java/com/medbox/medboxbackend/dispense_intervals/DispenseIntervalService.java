package com.medbox.medboxbackend.dispense_intervals;

import com.medbox.medboxbackend.model.DispenseInterval;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DispenseIntervalService {
    private final DispenseIntervalRepository dispenseIntervalRepository;

    public DispenseIntervalService(DispenseIntervalRepository dispenseIntervalRepository) {
        this.dispenseIntervalRepository = dispenseIntervalRepository;
    }

    public DispenseInterval updateDispenseInterval(Long id, long interval, long startTime, int pillsToDispense, String userId) {
        Optional<DispenseInterval> intervalOpt = dispenseIntervalRepository.findByIdAndUserId(id, userId);
        if (intervalOpt.isEmpty()) {
            throw new IllegalArgumentException("DispenseInterval with id " + id + " not found for user " + userId);
        }

        DispenseInterval dispenseInterval = intervalOpt.get();
        dispenseInterval.setInterval(interval);
        dispenseInterval.setStartTime(startTime);
        dispenseInterval.setPillsToDispense(pillsToDispense);

        return dispenseIntervalRepository.save(dispenseInterval);
    }

    public void deleteDispenseInterval(Long id, String userId) {
        Optional<DispenseInterval> intervalOpt = dispenseIntervalRepository.findByIdAndUserId(id, userId);
        if (intervalOpt.isEmpty()) {
            throw new IllegalArgumentException("DispenseInterval with id " + id + " not found for user " + userId);
        }
        dispenseIntervalRepository.delete(intervalOpt.get());
    }
}

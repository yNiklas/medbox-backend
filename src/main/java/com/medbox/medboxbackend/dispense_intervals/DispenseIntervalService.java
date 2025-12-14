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

    public DispenseInterval createDispenseInterval(Long compartmentId, long interval, long startTime, int pillsToDispense, String userId) {
        if (interval <= 0) throw new IllegalArgumentException("Interval must be positive");
        if (pillsToDispense <= 0) throw new IllegalArgumentException("Pills to dispense must be positive");
        if (startTime < 0) throw new IllegalArgumentException("Start time cannot be negative");

        DispenseInterval dispenseInterval = new DispenseInterval();
        dispenseInterval.setInterval(interval);
        dispenseInterval.setStartTime(startTime);
        dispenseInterval.setPillsToDispense(pillsToDispense);

        return dispenseIntervalRepository.save(dispenseInterval);
    }

    public DispenseInterval updateDispenseInterval(Long id, long interval, long startTime, int pillsToDispense, String userId) {
        Optional<DispenseInterval> intervalOpt = dispenseIntervalRepository.findByIdAndUserId(id, userId);
        if (intervalOpt.isEmpty()) {
            throw new IllegalArgumentException("DispenseInterval with id " + id + " not found for user " + userId);
        }

        if (interval <= 0) throw new IllegalArgumentException("Interval must be positive");
        if (pillsToDispense <= 0) throw new IllegalArgumentException("Pills to dispense must be positive");
        if (startTime < 0) throw new IllegalArgumentException("Start time cannot be negative");

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

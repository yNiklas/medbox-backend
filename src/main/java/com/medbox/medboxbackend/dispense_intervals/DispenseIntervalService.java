package com.medbox.medboxbackend.dispense_intervals;

import com.medbox.medboxbackend.compartments.CompartmentRepository;
import com.medbox.medboxbackend.compartments.CompartmentService;
import com.medbox.medboxbackend.model.Compartment;
import com.medbox.medboxbackend.model.DispenseInterval;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DispenseIntervalService {
    private final DispenseIntervalRepository dispenseIntervalRepository;
    private final CompartmentService compartmentService;
    private final CompartmentRepository compartmentRepository;

    public DispenseIntervalService(DispenseIntervalRepository dispenseIntervalRepository, CompartmentService compartmentService, CompartmentRepository compartmentRepository) {
        this.dispenseIntervalRepository = dispenseIntervalRepository;
        this.compartmentService = compartmentService;
        this.compartmentRepository = compartmentRepository;
    }

    public void createDispenseInterval(Long compartmentId, long interval, long startTime, int pillsToDispense, String userId) {
        Optional<Compartment> compartmentOpt = compartmentService.getCompartmentByIdAndUserId(compartmentId, userId);
        if (compartmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Compartment with id " + compartmentId + " not found for user " + userId);
        }

        if (interval <= 0) throw new IllegalArgumentException("Interval must be positive");
        if (pillsToDispense <= 0) throw new IllegalArgumentException("Pills to dispense must be positive");
        if (startTime < 0) throw new IllegalArgumentException("Start time cannot be negative");

        DispenseInterval dispenseInterval = new DispenseInterval();
        dispenseInterval.setInterval(interval);
        dispenseInterval.setStartTime(startTime);
        dispenseInterval.setPillsToDispense(pillsToDispense);

        compartmentOpt.get().addInterval(dispenseInterval);

        compartmentRepository.save(compartmentOpt.get());
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

package com.medbox.medboxbackend.dispense_intervals;

import com.medbox.medboxbackend.boxes.MedBoxDispenseSchedulerService;
import com.medbox.medboxbackend.compartments.CompartmentRepository;
import com.medbox.medboxbackend.compartments.CompartmentService;
import com.medbox.medboxbackend.exceptions.NoSuchResourceException;
import com.medbox.medboxbackend.model.Compartment;
import com.medbox.medboxbackend.model.DispenseInterval;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStack;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DispenseIntervalService {
    private final DispenseIntervalRepository dispenseIntervalRepository;
    private final CompartmentService compartmentService;
    private final CompartmentRepository compartmentRepository;
    private final MedBoxDispenseSchedulerService medBoxDispenseSchedulerService;

    public DispenseIntervalService(DispenseIntervalRepository dispenseIntervalRepository, CompartmentService compartmentService, CompartmentRepository compartmentRepository, MedBoxDispenseSchedulerService medBoxDispenseSchedulerService) {
        this.dispenseIntervalRepository = dispenseIntervalRepository;
        this.compartmentService = compartmentService;
        this.compartmentRepository = compartmentRepository;
        this.medBoxDispenseSchedulerService = medBoxDispenseSchedulerService;
    }

    public void createDispenseInterval(Long compartmentId, long interval, long startTime, int pillsToDispense, String userId) {
        Optional<Compartment> compartmentOpt = compartmentService.getCompartmentByIdAndUserId(compartmentId, userId);
        if (compartmentOpt.isEmpty()) {
            throw new NoSuchResourceException("Compartment with id " + compartmentId + " not found for user " + userId);
        }

        if (interval <= 0) throw new IllegalArgumentException("Interval must be positive");
        if (pillsToDispense <= 0) throw new IllegalArgumentException("Pills to dispense must be positive");
        if (startTime < 0) throw new IllegalArgumentException("Start time cannot be negative");

        DispenseInterval dispenseInterval = new DispenseInterval();
        dispenseInterval.setInterval(interval);
        dispenseInterval.setStartTime(startTime);
        dispenseInterval.setPillsToDispense(pillsToDispense);

        int intervalCount = compartmentOpt.get().intervalCount();
        compartmentOpt.get().appendInterval(dispenseInterval);

        Compartment savedCompartment = compartmentRepository.save(compartmentOpt.get());
        if (savedCompartment.intervalCount() > intervalCount) {
            DispenseInterval savedInterval = savedCompartment.getIntervals().get(intervalCount);
            scheduleInterval(savedInterval);
        }
    }

    public DispenseInterval updateDispenseInterval(Long id, long interval, long startTime, int pillsToDispense, String userId) {
        Optional<DispenseInterval> intervalOpt = dispenseIntervalRepository.findByIdAndUserId(id, userId);
        if (intervalOpt.isEmpty()) {
            throw new NoSuchResourceException("DispenseInterval with id " + id + " not found for user " + userId);
        }

        if (interval <= 0) throw new IllegalArgumentException("Interval must be positive");
        if (pillsToDispense <= 0) throw new IllegalArgumentException("Pills to dispense must be positive");
        if (startTime < 0) throw new IllegalArgumentException("Start time cannot be negative");

        DispenseInterval dispenseInterval = intervalOpt.get();
        dispenseInterval.setInterval(interval);
        dispenseInterval.setStartTime(startTime);
        dispenseInterval.setPillsToDispense(pillsToDispense);

        scheduleInterval(dispenseInterval);

        return dispenseIntervalRepository.save(dispenseInterval);
    }

    public void deleteDispenseInterval(Long id, String userId) {
        Optional<DispenseInterval> intervalOpt = dispenseIntervalRepository.findByIdAndUserId(id, userId);
        if (intervalOpt.isEmpty()) {
            throw new NoSuchResourceException("DispenseInterval with id " + id + " not found for user " + userId);
        }
        dispenseIntervalRepository.delete(intervalOpt.get());
        medBoxDispenseSchedulerService.removeScheduledDispenseByDispenseIntervalId(id);
    }

    private void scheduleInterval(DispenseInterval dispenseInterval) {
        Long id = dispenseInterval.getId();
        Optional<MedBoxStack> stackOpt = dispenseIntervalRepository.findStackByDispenseIntervalId(id);
        stackOpt.ifPresent(medBoxStack -> {
            Optional<MedBox> mosOpt = medBoxStack.getMos();
            Optional<MedBox> dispenseBoxOpt = medBoxStack.findMedBoxByDispenseIntervalId(id);
            if (dispenseBoxOpt.isEmpty()) {
                throw new IllegalStateException("MedBox for DispenseInterval id " + id + " not found in stack");
            }
            Optional<Integer> compartmentIndexOpt = dispenseBoxOpt.get().getCompartmentPositionByDispenseIntervalId(id);
            if (compartmentIndexOpt.isEmpty()) {
                throw new IllegalStateException("Compartment for DispenseInterval id " + id + " not found in MedBox " + dispenseBoxOpt.get().getMac());
            }
            mosOpt.ifPresent(mos -> medBoxDispenseSchedulerService.rescheduleDispenseInterval(
                    mos.getMac(),
                    dispenseBoxOpt.get().getMac(),
                    compartmentIndexOpt.get(),
                    dispenseInterval
            ));
        });
    }
}

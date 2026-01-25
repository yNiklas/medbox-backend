package com.medbox.medboxbackend.boxes;

import com.medbox.medboxbackend.model.Compartment;
import com.medbox.medboxbackend.model.DispenseInterval;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.stacks.MedBoxStackService;
import com.medbox.medboxbackend.websocket.service.DeviceWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MedBoxDispenseSchedulerService {
    private final Logger logger = LoggerFactory.getLogger(MedBoxDispenseSchedulerService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final DeviceWebSocketService deviceWebSocketService;
    private final MedBoxStackService medBoxStackService;

    private final List<ScheduledBoxDispense> scheduledBoxDispenses = new ArrayList<>();
    private final MedBoxService medBoxService;

    public MedBoxDispenseSchedulerService(DeviceWebSocketService deviceWebSocketService, MedBoxStackService medBoxStackService, MedBoxService medBoxService) {
        this.deviceWebSocketService = deviceWebSocketService;
        this.medBoxStackService = medBoxStackService;
        this.medBoxService = medBoxService;
    }

    public void reScheduleStack(String mosMac) {
        Optional<List<MedBox>> boxesOpt = medBoxStackService.getBoxesOfStack(mosMac);
        if (boxesOpt.isEmpty()) {
            logger.warn("No boxes found for stack with MOS MAC: {}", mosMac);
            return;
        }

        removeScheduledDispenseByMosMac(mosMac);

        boxesOpt.get().forEach(box -> {
            List<Compartment> compartments = box.getCompartments();
            for (Compartment compartment : compartments) {
                for (DispenseInterval interval : compartment.getIntervals()) {
                    scheduleDispense(mosMac, box.getMac(), compartment.getPosition(), interval);
                }
            }
        });
    }

    public void rescheduleDispenseInterval(String mosMac, String boxMac, int compartmentPosition, DispenseInterval interval) {
        removeScheduledDispenseByDispenseIntervalId(interval.getId());
        scheduleDispense(mosMac, boxMac, compartmentPosition, interval);
    }

    private void scheduleDispense(String mosMac, String boxMac, int compartmentPosition, DispenseInterval interval) {
        logger.info("Scheduling dispense for device: {}, box: {}, compartment: {}, interval: from {} UTC every {}ms; next dispense: {} UTC",
                mosMac, boxMac, compartmentPosition, interval.getStartTime(), interval.getInterval(), interval.getNextDispenseTime());
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(
                () -> {
                    if (deviceWebSocketService.isDeviceConnected(mosMac)) {
                        try {
                            deviceWebSocketService.requestDispense(mosMac, boxMac, compartmentPosition, interval.getPillsToDispense());
                            medBoxService.registerDispensedPills(boxMac, compartmentPosition, interval.getPillsToDispense());
                        } catch (Exception e) {
                            logger.error("Error on executing dispense: " + e.getMessage());
                        }
                    } else {
                        logger.warn("Cannot schedule dispense for disconnected device: {}", mosMac);
                    }
                },
                interval.getNextDispenseTime() - System.currentTimeMillis(),
                interval.getInterval(),
                TimeUnit.MILLISECONDS
        );
        scheduledBoxDispenses.add(new ScheduledBoxDispense(mosMac, boxMac, compartmentPosition, interval.getId(), scheduledFuture));
    }

    public void removeScheduledDispenseByMosMac(String mosMac) {
        scheduledBoxDispenses.removeIf(scheduledDispense -> {
            if (scheduledDispense.isOfStack(mosMac)) {
                scheduledDispense.scheduledFuture().cancel(false);
                return true;
            }
            return false;
        });
    }

    public void removeScheduledDispenseByDispenseIntervalId(Long intervalId) {
        scheduledBoxDispenses.removeIf(scheduledDispense -> {
            if (scheduledDispense.isOfInterval(intervalId)) {
                scheduledDispense.scheduledFuture().cancel(false);
                return true;
            }
            return false;
        });
    }
}

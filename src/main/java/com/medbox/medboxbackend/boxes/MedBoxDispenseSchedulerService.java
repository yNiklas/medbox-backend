package com.medbox.medboxbackend.boxes;

import com.medbox.medboxbackend.model.Compartment;
import com.medbox.medboxbackend.model.DispenseInterval;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStack;
import com.medbox.medboxbackend.notifications.PushNotificationService;
import com.medbox.medboxbackend.robots.RobotService;
import com.medbox.medboxbackend.stacks.MedBoxStackRepository;
import com.medbox.medboxbackend.stacks.MedBoxStackService;
import com.medbox.medboxbackend.websocket.service.DeviceWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final MedBoxService medBoxService;
    private final RobotService robotService;
    private final LocationService locationService;

    private final List<ScheduledBoxDispense> scheduledBoxDispenses = new ArrayList<>();
    private final PushNotificationService pushNotificationService;
    private final MedBoxStackRepository medBoxStackRepository;

    public MedBoxDispenseSchedulerService(DeviceWebSocketService deviceWebSocketService, MedBoxStackService medBoxStackService, MedBoxService medBoxService, RobotService robotService, LocationService locationService, PushNotificationService pushNotificationService, MedBoxStackRepository medBoxStackRepository) {
        this.deviceWebSocketService = deviceWebSocketService;
        this.medBoxStackService = medBoxStackService;
        this.medBoxService = medBoxService;
        this.robotService = robotService;
        this.locationService = locationService;
        this.pushNotificationService = pushNotificationService;
        this.medBoxStackRepository = medBoxStackRepository;
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
                () -> executeDispense(mosMac, boxMac, compartmentPosition, interval),
                interval.getNextDispenseTime() - System.currentTimeMillis(),
                interval.getInterval(),
                TimeUnit.MILLISECONDS
        );
        scheduledBoxDispenses.add(new ScheduledBoxDispense(mosMac, boxMac, compartmentPosition, interval.getId(), scheduledFuture));
    }

    private void executeDispense(String mosMac, String boxMac, int compartmentPosition, DispenseInterval interval) {
        if (!deviceWebSocketService.isDeviceConnected(mosMac)) {
            logger.warn("Cannot execute dispense for disconnected device: {}", mosMac);
            return;
        }

        Optional<MedBoxStack> stackOpt = medBoxStackRepository.findMedBoxStackByMedBoxMACAddress(mosMac);
        if (stackOpt.isEmpty()) {
            logger.error("MedBoxStack with MOS MAC {} not found", mosMac);
            return;
        }
        Optional<MedBox> boxOpt = stackOpt.get().getBoxes().stream().filter(box -> box.getMac().equals(boxMac)).findFirst();
        if (boxOpt.isEmpty()) {
            logger.error("MedBox with MAC {} not found in stack {}", boxMac, mosMac);
            return;
        }

        MedBox box = boxOpt.get();
        
        // Check if location and robot are specified
        if (box.getLocation() != null && box.getRobot() != null) {
            if (!sendGoalAndWaitForRobot(stackOpt.get().getUserId(), box)) {
                logger.warn("Robot did not reach location for box {}. Skipping dispense.", boxMac);
                return;
            }
        }

        try {
            deviceWebSocketService.requestDispense(mosMac, boxMac, compartmentPosition, interval.getPillsToDispense());
            medBoxService.registerDispensedPills(boxMac, compartmentPosition, interval.getPillsToDispense());
            logger.info("Dispense executed for box: {}, compartment: {}, pills: {}", boxMac, compartmentPosition, interval.getPillsToDispense());
        } catch (Exception e) {
            logger.error("Error on executing dispense: " + e.getMessage());
        }
    }

    private boolean sendGoalAndWaitForRobot(String userId, MedBox box) {
        String location = box.getLocation();
        String robotName = box.getRobot();
        
        if (location == null || robotName == null) {
            return true; // No location/robot specified, proceed with dispense
        }

        if (!robotService.isRobotConnected(robotName)) {
            logger.warn("Robot {} is not connected. Skipping goal-based dispense for box {}", robotName, box.getMac());
            pushNotificationService.sendNotification(userId, "Dispense failed","Robot " + robotName + " is not connected. Dispense for box " + box.getName() + " skipped.");
            return false;
        }

        Map<String, Integer> coords = locationService.getLocationCoordinates(location);
        if (coords == null) {
            logger.error("Location {} not found for box {}", location, box.getMac());
            return false;
        }

        double x = coords.getOrDefault("x", 0);
        double y = coords.getOrDefault("y", 0);
        double z = coords.getOrDefault("z", 0);

        try {
            logger.info("Sending goal to robot {} for box {} at location {} ({}, {}, {})", robotName, box.getMac(), location, x, y, z);
            robotService.sendGoal(robotName, x, y, z, 0.0);
            
            logger.info("Waiting for robot {} to reach location {}", robotName, location);
            boolean arrived = robotService.waitForRobotAtLocation(robotName, x, y, z);
            
            if (!arrived) {
                logger.warn("Robot {} failed to reach location {} within timeout", robotName, location);
                pushNotificationService.sendNotification(userId, "Dispense failed","Robot " + robotName + " did not reach location " + location + " in time. Dispense for box " + box.getName() + " skipped.");
            }
            
            return arrived;
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for robot {}: {}", robotName, e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            logger.error("Error sending goal to robot {}: {}", robotName, e.getMessage());
            return false;
        }
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

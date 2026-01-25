package com.medbox.medboxbackend.compartments;

import com.medbox.medboxbackend.exceptions.NoSuchResourceException;
import com.medbox.medboxbackend.model.Compartment;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStack;
import com.medbox.medboxbackend.websocket.service.DeviceWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Service
public class CompartmentService {
    private static final Logger logger = LoggerFactory.getLogger(CompartmentService.class);

    private final CompartmentRepository compartmentRepository;
    private final DeviceWebSocketService deviceWebSocketService;

    public CompartmentService(CompartmentRepository compartmentRepository, DeviceWebSocketService deviceWebSocketService) {
        this.compartmentRepository = compartmentRepository;
        this.deviceWebSocketService = deviceWebSocketService;
    }

    public Optional<Compartment> getCompartmentByIdAndUserId(Long id, String userId) {
        return compartmentRepository.findByIdAndUserId(id, userId);
    }

    public Compartment renameCompartment(Long id, String newName, String userId) {
        Optional<Compartment> compartmentOpt = compartmentRepository.findByIdAndUserId(id, userId);
        if (compartmentOpt.isEmpty()) {
            throw new NoSuchResourceException("Compartment with id " + id + " not found for user " + userId);
        }

        if (Objects.equals(compartmentOpt.get().getName(), newName)) {
            return compartmentOpt.get();
        }

        if (compartmentRepository.existsNameInSameMedBox(newName, id)) {
            throw new IllegalArgumentException("Compartment with name " + newName + " already exists in the same MedBox.");
        }

        Compartment compartment = compartmentOpt.get();
        compartment.setName(newName);

        return compartmentRepository.save(compartment);
    }

    public void deleteCompartment(Long id, String userId) {
        Optional<Compartment> compartmentOpt = compartmentRepository.findByIdAndUserId(id, userId);
        if (compartmentOpt.isEmpty()) {
            throw new NoSuchResourceException("Compartment with id " + id + " not found for user " + userId);
        }
        compartmentRepository.delete(compartmentOpt.get());
    }

    public Compartment refillCompartment(Long id, int pillsToAdd, String userId) {
        Optional<Compartment> compartmentOpt = compartmentRepository.findByIdAndUserId(id, userId);
        if (compartmentOpt.isEmpty()) {
            throw new NoSuchResourceException("Compartment with id " + id + " not found for user " + userId);
        }

        Compartment compartment = compartmentOpt.get();
        int newRemainingPills = compartment.getRemainingPills() + pillsToAdd;
        
        // Ensure remainingPills stays >= 0
        if (newRemainingPills < 0) {
            newRemainingPills = 0;
        }
        
        compartment.setRemainingPills(newRemainingPills);
        requestFunnelSpotChange(id);

        return compartmentRepository.save(compartment);
    }

    private void requestFunnelSpotChange(Long id) {
        Optional<MedBoxStack> stackOfCompartmentOpt = compartmentRepository.findStackOfCompartment(id);
        if (stackOfCompartmentOpt.isPresent()) {
            Optional<MedBox> mosOpt = stackOfCompartmentOpt.get().getMos();
            if (mosOpt.isPresent()) {
                Optional<MedBox> targetBoxOpt = stackOfCompartmentOpt.get().findMedBoxByCompartmentId(id);
                if (targetBoxOpt.isPresent()) {
                    Optional<Integer> targetCompartmentPositionOpt = targetBoxOpt.get().getCompartmentPositionById(id);
                    if (targetCompartmentPositionOpt.isPresent()) {
                        try {
                            deviceWebSocketService.requestFunnelSpotChange(
                                    mosOpt.get().getMac(),
                                    targetBoxOpt.get().getMac(),
                                    targetCompartmentPositionOpt.get()
                            );
                        } catch (IOException e) {
                            logger.error("Failed to send funnel spot change request for compartment id {}", id, e);
                        }
                    } else {
                        logger.warn("Target compartment number not found in MedBox for compartment id {}", id);
                    }
                } else {
                    logger.warn("Target MedBox not found in MedBoxStack for compartment id {}", id);
                }
            } else {
                logger.warn("MOS not found in MedBoxStack for compartment id {}", id);
            }
        } else {
            logger.warn("MedBoxStack not found for compartment id {}", id);
        }
    }
}

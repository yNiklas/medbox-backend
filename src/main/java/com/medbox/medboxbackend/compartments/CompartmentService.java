package com.medbox.medboxbackend.compartments;

import com.medbox.medboxbackend.model.Compartment;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class CompartmentService {
    private final CompartmentRepository compartmentRepository;

    public CompartmentService(CompartmentRepository compartmentRepository) {
        this.compartmentRepository = compartmentRepository;
    }

    public Optional<Compartment> getCompartmentByIdAndUserId(Long id, String userId) {
        return compartmentRepository.findByIdAndUserId(id, userId);
    }

    public Compartment renameCompartment(Long id, String newName, String userId) {
        Optional<Compartment> compartmentOpt = compartmentRepository.findByIdAndUserId(id, userId);
        if (compartmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Compartment with id " + id + " not found for user " + userId);
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
            throw new IllegalArgumentException("Compartment with id " + id + " not found for user " + userId);
        }
        compartmentRepository.delete(compartmentOpt.get());
    }

    public Compartment refillCompartment(Long id, int pillsToAdd, String userId) {
        Optional<Compartment> compartmentOpt = compartmentRepository.findByIdAndUserId(id, userId);
        if (compartmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Compartment with id " + id + " not found for user " + userId);
        }

        Compartment compartment = compartmentOpt.get();
        int newRemainingPills = compartment.getRemainingPills() + pillsToAdd;
        
        // Ensure remainingPills stays >= 0
        if (newRemainingPills < 0) {
            newRemainingPills = 0;
        }
        
        compartment.setRemainingPills(newRemainingPills);
        return compartmentRepository.save(compartment);
    }
}

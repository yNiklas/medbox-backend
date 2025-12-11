package com.medbox.medboxbackend.stacks;

import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStack;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Service
public class MedBoxStackService {
    private final MedBoxStackRepository medBoxStackRepository;

    public MedBoxStackService(MedBoxStackRepository medBoxStackRepository) {
        this.medBoxStackRepository = medBoxStackRepository;
    }

    public List<MedBoxStack> getOwnStacks(Principal principal) {
        if (principal == null) return Collections.emptyList();
        return medBoxStackRepository.findAllByUserId(principal.getName());
    }

    public void assignMedBoxStackByMasterMACAddress(String masterMACAddress, String boxName, String stackName, Principal principal) {
        // Check if MedBox is already assigned
        if (medBoxStackRepository.findMedBoxStackByMedBoxMACAddress(masterMACAddress).isPresent()) {
            throw new IllegalArgumentException("MedBox with MAC address " + masterMACAddress + " is already assigned.");
        }

        // Check if name is already used
        if (medBoxStackRepository.findByNameAndUserId(stackName, principal.getName()).isPresent()) {
            throw new IllegalArgumentException("Stack with name " + stackName + " already exists.");
        }

        // Create new box and stack
        MedBox masterBox = new MedBox(masterMACAddress, boxName);
        MedBoxStack stack = new MedBoxStack(stackName, masterBox, principal.getName());
        medBoxStackRepository.save(stack);
    }

    public MedBoxStack getMedBoxStackByIdAndUserId(Long id, String userId) {
        return medBoxStackRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("MedBoxStack with id " + id + " not found for user " + userId));
    }
}

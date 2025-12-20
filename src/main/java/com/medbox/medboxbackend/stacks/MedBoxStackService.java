package com.medbox.medboxbackend.stacks;

import com.medbox.medboxbackend.boxes.MedBoxService;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStack;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MedBoxStackService {
    private final MedBoxStackRepository medBoxStackRepository;
    private final MedBoxService medBoxService;

    public MedBoxStackService(MedBoxStackRepository medBoxStackRepository, MedBoxService medBoxService) {
        this.medBoxStackRepository = medBoxStackRepository;
        this.medBoxService = medBoxService;
    }

    public List<MedBoxStack> getOwnStacks(Principal principal) {
        if (principal == null) return Collections.emptyList();
        return medBoxStackRepository.findAllByUserId(principal.getName());
    }

    public MedBoxStack assignMedBoxStackByMasterMACAddress(String masterMACAddress, String boxName, String stackName, String userId) {
        // Check if MedBox is already assigned
        if (medBoxStackRepository.findMedBoxStackByMedBoxMACAddress(masterMACAddress).isPresent()) {
            throw new IllegalArgumentException("MedBox with MAC address " + masterMACAddress + " is already assigned.");
        }

        // Check if name is already used
        if (medBoxStackRepository.findByNameAndUserId(stackName, userId).isPresent()) {
            throw new IllegalArgumentException("Stack with name " + stackName + " already exists.");
        }

        // Create new box and stack
        MedBox masterBox = new MedBox(masterMACAddress, boxName);
        MedBoxStack stack = new MedBoxStack(stackName, masterBox, userId);
        return medBoxStackRepository.save(stack);
    }

    public Optional<MedBoxStack> getMedBoxStackByIdAndUserId(Long id, String userId) {
        return medBoxStackRepository.findByIdAndUserId(id, userId);
    }

    public Optional<List<MedBox>> getBoxesOfStack(String masterMACAddress) {
        return medBoxStackRepository.findMedBoxStackByMedBoxMACAddress(masterMACAddress)
                .map(MedBoxStack::getBoxes);
    }

    public Iterable<MedBoxStack> getAllStacks() {
        return medBoxStackRepository.findAll();
    }

    public MedBoxStack renameMedBoxStack(Long id, String newName, String userId) {
        Optional<MedBoxStack> stackOpt = medBoxStackRepository.findByIdAndUserId(id, userId);
        if (stackOpt.isEmpty()) {
            throw new IllegalArgumentException("MedBoxStack with id " + id + " not found for user " + userId);
        }

        // Check if new name is already used
        if (medBoxStackRepository.findByNameAndUserId(newName, userId).isPresent()) {
            throw new IllegalArgumentException("Stack with name " + newName + " already exists.");
        }

        MedBoxStack stack = stackOpt.get();
        stack.setName(newName);

        return medBoxStackRepository.save(stack);
    }

    public void deleteMedBoxStackById(Long id, String userId) {
        Optional<MedBoxStack> stackOpt = medBoxStackRepository.findByIdAndUserId(id, userId);
        if (stackOpt.isEmpty()) {
            throw new IllegalArgumentException("MedBoxStack with id " + id + " not found for user " + userId);
        } else {
            medBoxStackRepository.delete(stackOpt.get());
        }
    }

    public void registerStackOrder(String masterMAC, List<String> macsInPhysicalOrder) {
        Optional<MedBoxStack> stackOpt = medBoxStackRepository.findMedBoxStackByMedBoxMACAddress(masterMAC);
        if (stackOpt.isEmpty()) {
            throw new IllegalArgumentException("MedBoxStack with master MAC " + masterMAC + " not found.");
        }

        stackOpt.get().updateOrder(macsInPhysicalOrder);
    }

    public void updateOnlineStatusOfStackByMasterMAC(String masterMAC) {
        Optional<MedBoxStack> stackOpt = medBoxStackRepository.findMedBoxStackByMedBoxMACAddress(masterMAC);
        if (stackOpt.isEmpty()) {
            throw new IllegalArgumentException("MedBoxStack with master MAC " + masterMAC + " not found.");
        }

        MedBoxStack stack = stackOpt.get();
        if (stack.getBoxes() != null) {
            for (MedBox box : stack.getBoxes()) {
                box.updateOnlineStatus();
            }

            medBoxStackRepository.save(stack);
        }
    }

    public void handleStackError(int errorType, String content) {
        // Until now: Content is always the MAC address of the affected box
        medBoxService.registerMedBoxError(content, "The box is no longer connected (error code " + errorType + ").");
    }

    public MedBox onboardSlaveMedBox(Long stackId, String slaveBoxMac, String slaveBoxName, String userId) {
        Optional<MedBoxStack> stackOpt = medBoxStackRepository.findByIdAndUserId(stackId, userId);
        if (stackOpt.isEmpty()) {
            throw new IllegalArgumentException("MedBoxStack with id " + stackId + " not found for user " + userId);
        }

        return stackOpt.get().onboardSlaveMedBox(slaveBoxMac, slaveBoxName);
    }
}

package com.medbox.medboxbackend.boxes;

import com.medbox.medboxbackend.exceptions.NoSuchResourceException;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class MedBoxService {
    private final MedBoxRepository medBoxRepository;
    private final LocationService locationService;

    public MedBoxService(MedBoxRepository medBoxRepository, LocationService locationService) {
        this.medBoxRepository = medBoxRepository;
        this.locationService = locationService;
    }

    public Optional<MedBox> getMedBoxByIdAndUserId(Long id, String userId) {
        return medBoxRepository.findByIdAndUserId(id, userId);
    }

    public MedBox renameMedBox(Long id, String newName, String userId) {
        Optional<MedBox> boxOpt = medBoxRepository.findByIdAndUserId(id, userId);
        if (boxOpt.isEmpty()) {
            throw new NoSuchResourceException("MedBox with id " + id + " not found for user " + userId);
        }

        if (Objects.equals(boxOpt.get().getName(), newName)) {
            return boxOpt.get();
        }

        if (medBoxRepository.existsNameInSameStack(newName, id)) {
            throw new IllegalArgumentException("MedBox with name " + newName + " already exists in the stack.");
        }

        MedBox box = boxOpt.get();
        box.setName(newName);

        return medBoxRepository.save(box);
    }

    public void deleteMedBox(Long id, String userId) {
        Optional<MedBox> boxOpt = medBoxRepository.findByIdAndUserId(id, userId);
        if (boxOpt.isEmpty()) {
            throw new NoSuchResourceException("MedBox with id " + id + " not found for user " + userId);
        } else {
            medBoxRepository.delete(boxOpt.get());
        }
    }

    public MedBox updateMedBoxLocation(Long id, String location, String userId) {
        Optional<MedBox> boxOpt = medBoxRepository.findByIdAndUserId(id, userId);
        if (boxOpt.isEmpty()) {
            throw new NoSuchResourceException("MedBox with id " + id + " not found for user " + userId);
        }

        if (!locationService.isValidLocation(location)) {
            throw new IllegalArgumentException("Invalid location: " + location + ". Location not found in available locations.");
        }

        MedBox box = boxOpt.get();
        box.setLocation(location);

        return medBoxRepository.save(box);
    }

    public void registerMedBoxError(String mac, String error) {
        Optional<MedBox> boxOpt = medBoxRepository.findByMac(mac);
        if (boxOpt.isEmpty()) {
            throw new IllegalArgumentException("MedBox with MAC " + mac + " not found.");
        }

        boxOpt.get().registerError(error);

        medBoxRepository.save(boxOpt.get());
    }

    public void registerDispensedPills(String boxMac, int compartmentPosition, int amountOfPills) {
        Optional<MedBox> boxOpt = medBoxRepository.findByMac(boxMac);
        if (boxOpt.isEmpty()) {
            throw new IllegalArgumentException("MedBox with MAC " + boxMac + " not found.");
        }

        MedBox box = boxOpt.get();
        box.registerDispensedPills(compartmentPosition, amountOfPills);

        medBoxRepository.save(box);
    }
}

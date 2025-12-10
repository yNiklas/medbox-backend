package com.medbox.mexboxbackend.stacks;

import com.medbox.mexboxbackend.model.MedBoxStack;
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
}

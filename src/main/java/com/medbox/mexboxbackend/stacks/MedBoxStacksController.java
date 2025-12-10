package com.medbox.mexboxbackend.stacks;

import com.medbox.mexboxbackend.model.MedBoxStack;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stacks")
public class MedBoxStacksController {
    private final MedBoxStackService medBoxStackService;

    public MedBoxStacksController(MedBoxStackService medBoxStackService) {
        this.medBoxStackService = medBoxStackService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<MedBoxStack> getMedBoxStacks(Principal principal) {
        return medBoxStackService.getOwnStacks(principal);
    }
}

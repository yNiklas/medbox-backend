package com.medbox.medboxbackend.stacks;

import com.medbox.medboxbackend.model.MedBoxStack;
import com.medbox.medboxbackend.stacks.requests.AssignMedBoxStackRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public void assignMedBoxStackByMasterMACAddress(@RequestBody AssignMedBoxStackRequest request,
                                                    Principal principal) {
        medBoxStackService.assignMedBoxStackByMasterMACAddress(request.masterMACAddress(),
                request.boxName(), request.stackName(), principal);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public MedBoxStack getMedBoxStackById(@PathVariable Long id, Principal principal) {
        return medBoxStackService.getMedBoxStackByIdAndUserId(id, principal.getName());
    }
}

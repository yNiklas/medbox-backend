package com.medbox.medboxbackend.stacks;

import com.medbox.medboxbackend.exceptions.NoSuchResourceException;
import com.medbox.medboxbackend.model.MedBoxStack;
import com.medbox.medboxbackend.stacks.requests.AssignMedBoxStackRequest;
import com.medbox.medboxbackend.stacks.requests.RenameMedBoxStackRequest;
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
    public MedBoxStack assignMedBoxStackByMasterMACAddress(@RequestBody AssignMedBoxStackRequest request,
                                                    Principal principal) {
        return medBoxStackService.assignMedBoxStackByMasterMACAddress(request.masterMACAddress(),
                request.boxName(), request.stackName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public MedBoxStack getMedBoxStackById(@PathVariable Long id, Principal principal) {
        return medBoxStackService.getMedBoxStackByIdAndUserId(id, principal.getName())
                .orElseThrow(() -> new NoSuchResourceException("MedBoxStack with id " + id + " not found for user " + principal.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/name")
    public MedBoxStack renameMedBoxStack(@PathVariable Long id, @RequestBody RenameMedBoxStackRequest request, Principal principal) {
        return medBoxStackService.renameMedBoxStack(id, request.updatedName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public void deleteMedBoxStackById(@PathVariable Long id, Principal principal) {
        medBoxStackService.deleteMedBoxStackById(id, principal.getName());
    }
}

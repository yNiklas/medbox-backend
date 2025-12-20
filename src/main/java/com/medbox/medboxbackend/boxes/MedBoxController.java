package com.medbox.medboxbackend.boxes;

import com.medbox.medboxbackend.boxes.requests.OnboardSlaveRequest;
import com.medbox.medboxbackend.boxes.requests.RenameMedBoxRequest;
import com.medbox.medboxbackend.model.MedBox;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/boxes")
public class MedBoxController {
    private final MedBoxService medBoxService;

    public MedBoxController(MedBoxService medBoxService) {
        this.medBoxService = medBoxService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public MedBox getMedBoxById(@PathVariable Long id, Principal principal) {
        return medBoxService.getMedBoxByIdAndUserId(id, principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("MedBox with id " + id + " not found for user " + principal.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/name")
    public MedBox renameMedBox(@PathVariable Long id, @RequestBody RenameMedBoxRequest request, Principal principal) {
        return medBoxService.renameMedBox(id, request.updatedName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public void deleteMedBoxById(@PathVariable Long id, Principal principal) {
        medBoxService.deleteMedBox(id, principal.getName());
    }
}

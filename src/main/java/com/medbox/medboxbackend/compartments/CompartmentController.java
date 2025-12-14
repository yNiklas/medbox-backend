package com.medbox.medboxbackend.compartments;

import com.medbox.medboxbackend.compartments.requests.RenameCompartmentRequest;
import com.medbox.medboxbackend.model.Compartment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/compartments")
public class CompartmentController {
    private final CompartmentService compartmentService;

    public CompartmentController(CompartmentService compartmentService) {
        this.compartmentService = compartmentService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Compartment getCompartmentById(@PathVariable Long id, Principal principal) {
        return compartmentService.getCompartmentByIdAndUserId(id, principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Compartment with id " + id + " not found for user " + principal.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/name")
    public Compartment renameCompartment(@PathVariable Long id, @RequestBody RenameCompartmentRequest request, Principal principal) {
        return compartmentService.renameCompartment(id, request.updatedName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public void deleteCompartmentById(@PathVariable Long id, Principal principal) {
        compartmentService.deleteCompartment(id, principal.getName());
    }
}

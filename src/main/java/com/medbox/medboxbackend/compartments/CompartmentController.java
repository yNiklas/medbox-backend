package com.medbox.medboxbackend.compartments;

import com.medbox.medboxbackend.compartments.requests.RenameCompartmentRequest;
import com.medbox.medboxbackend.model.Compartment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/compartments")
public class CompartmentController {
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Compartment getCompartmentById(@RequestParam Long id, Principal principal) {
        // TODO: Implementation to retrieve compartment by ID for the authenticated user
        return null; // Placeholder return statement
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/name")
    public Compartment renameCompartment(@PathVariable Long id, @RequestBody RenameCompartmentRequest request, Principal principal) {
        // TODO: Implementation to rename compartment for the authenticated user
        return null; // Placeholder return statement
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public void deleteCompartmentById(@PathVariable Long id, Principal principal) {
        // TODO: Implementation to delete compartment by ID for the authenticated user
    }
}

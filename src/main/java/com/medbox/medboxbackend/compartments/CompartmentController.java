package com.medbox.medboxbackend.compartments;

import com.medbox.medboxbackend.compartments.requests.RefillCompartmentRequest;
import com.medbox.medboxbackend.compartments.requests.RenameCompartmentRequest;
import com.medbox.medboxbackend.exceptions.NoSuchResourceException;
import com.medbox.medboxbackend.model.Compartment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Get Compartment by ID", description = "Returns the Compartment with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compartment found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Compartment not found for the user")
    })
    public Compartment getCompartmentById(@PathVariable Long id, Principal principal) {
        return compartmentService.getCompartmentByIdAndUserId(id, principal.getName())
                .orElseThrow(() -> new NoSuchResourceException("Compartment with id " + id + " not found for user " + principal.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/name")
    @Operation(summary = "Rename Compartment", description = "Renames the Compartment with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compartment renamed successfully"),
            @ApiResponse(responseCode = "404", description = "Compartment not found for the user"),
            @ApiResponse(responseCode = "400", description = "Compartment with the given name already exists in the same MedBox")
    })
    public Compartment renameCompartment(@PathVariable Long id, @RequestBody RenameCompartmentRequest request, Principal principal) {
        return compartmentService.renameCompartment(id, request.updatedName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Compartment", description = "Deletes the Compartment with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compartment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Compartment not found for the user")
    })
    public void deleteCompartmentById(@PathVariable Long id, Principal principal) {
        compartmentService.deleteCompartment(id, principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/refill")
    @Operation(summary = "Refill Compartment", description = "Refills the Compartment with the given ID by adding pills")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compartment refilled successfully"),
            @ApiResponse(responseCode = "404", description = "Compartment not found for the user")
    })
    public Compartment refillCompartment(@PathVariable Long id, @RequestBody RefillCompartmentRequest request, Principal principal) {
        return compartmentService.refillCompartment(id, request.pillsToAdd(), principal.getName());
    }
}

package com.medbox.medboxbackend.boxes;

import com.medbox.medboxbackend.boxes.requests.RenameMedBoxRequest;
import com.medbox.medboxbackend.boxes.requests.UpdateMedBoxLocationRequest;
import com.medbox.medboxbackend.exceptions.NoSuchResourceException;
import com.medbox.medboxbackend.model.MedBox;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boxes")
public class MedBoxController {
    private final MedBoxService medBoxService;
    private final LocationService locationService;

    public MedBoxController(MedBoxService medBoxService, LocationService locationService) {
        this.medBoxService = medBoxService;
        this.locationService = locationService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get MedBox by ID", description = "Returns the MedBox with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox not found for the user")
    })
    public MedBox getMedBoxById(@PathVariable Long id, Principal principal) {
        return medBoxService.getMedBoxByIdAndUserId(id, principal.getName())
                .orElseThrow(() -> new NoSuchResourceException("MedBox with id " + id + " not found for user " + principal.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/name")
    @Operation(summary = "Rename MedBox", description = "Renames the MedBox with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox renamed successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox not found for the user"),
            @ApiResponse(responseCode = "400", description = "A MedBox with the given name already exists in the stack")
    })
    public MedBox renameMedBox(@PathVariable Long id, @RequestBody RenameMedBoxRequest request, Principal principal) {
        return medBoxService.renameMedBox(id, request.updatedName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete MedBox", description = "Deletes the MedBox with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox deleted successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox not found for the user")
    })
    public void deleteMedBoxById(@PathVariable Long id, Principal principal) {
        medBoxService.deleteMedBox(id, principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/location")
    @Operation(summary = "Update MedBox Location", description = "Updates the location of the MedBox with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox location updated successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox not found for the user"),
            @ApiResponse(responseCode = "400", description = "Invalid location")
    })
    public MedBox updateMedBoxLocation(@PathVariable Long id, @RequestBody UpdateMedBoxLocationRequest request, Principal principal) {
        return medBoxService.updateMedBoxLocation(id, request.location(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/locations")
    @Operation(summary = "Get Available Locations", description = "Returns the list of available room locations")
    @ApiResponse(responseCode = "200", description = "List of available locations returned successfully")
    public List<String> getAvailableLocations() {
        return locationService.getAvailableLocations();
    }
}

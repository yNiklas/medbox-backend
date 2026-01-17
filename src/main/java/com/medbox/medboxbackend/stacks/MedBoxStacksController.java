package com.medbox.medboxbackend.stacks;

import com.medbox.medboxbackend.boxes.requests.OnboardSlaveRequest;
import com.medbox.medboxbackend.exceptions.NoSuchResourceException;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStack;
import com.medbox.medboxbackend.stacks.requests.AssignMedBoxStackRequest;
import com.medbox.medboxbackend.stacks.requests.RenameMedBoxStackRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Get all MedBox Stacks", description = "Returns all MedBox Stacks owned by the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox Stacks retrieved successfully")
    })
    public List<MedBoxStack> getMedBoxStacks(Principal principal) {
        return medBoxStackService.getOwnStacks(principal);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Assign MedBox Stack", description = "Assigns a MedBox Stack by master MAC address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox Stack assigned successfully"),
            @ApiResponse(responseCode = "400", description = "MedBox with MAC address is already assigned or stack with name already exists")
    })
    public MedBoxStack assignMedBoxStackByMasterMACAddress(@RequestBody AssignMedBoxStackRequest request,
                                                           Principal principal) {
        return medBoxStackService.assignMedBoxStackByMasterMACAddress(request.masterMACAddress(),
                request.boxName(), request.stackName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(summary = "Get MedBox Stack by ID", description = "Returns the MedBox Stack with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox Stack found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox Stack not found for the user")
    })
    public MedBoxStack getMedBoxStackById(@PathVariable Long id, Principal principal) {
        return medBoxStackService.getMedBoxStackByIdAndUserId(id, principal.getName())
                .orElseThrow(() -> new NoSuchResourceException("MedBoxStack with id " + id + " not found for user " + principal.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/name")
    @Operation(summary = "Rename MedBox Stack", description = "Renames the MedBox Stack with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox Stack renamed successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox Stack not found for the user"),
            @ApiResponse(responseCode = "400", description = "Stack with the given name already exists")
    })
    public MedBoxStack renameMedBoxStack(@PathVariable Long id, @RequestBody RenameMedBoxStackRequest request, Principal principal) {
        return medBoxStackService.renameMedBoxStack(id, request.updatedName(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete MedBox Stack", description = "Deletes the MedBox Stack with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MedBox Stack deleted successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox Stack not found for the user")
    })
    public void deleteMedBoxStackById(@PathVariable Long id, Principal principal) {
        medBoxStackService.deleteMedBoxStackById(id, principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/slave-onboarding")
    @Operation(summary = "Onboard Slave MedBox", description = "Onboards a slave MedBox to the stack")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slave MedBox onboarded successfully"),
            @ApiResponse(responseCode = "404", description = "MedBox Stack not found for the user")
    })
    public MedBox onboardSlaveMedBox(@PathVariable Long id, @RequestBody OnboardSlaveRequest request, Principal principal) {
        return medBoxStackService.onboardSlaveMedBox(id, request.boxMac(), request.boxName(), principal.getName());
    }
}

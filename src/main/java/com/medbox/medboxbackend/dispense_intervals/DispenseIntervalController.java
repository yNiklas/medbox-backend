package com.medbox.medboxbackend.dispense_intervals;

import com.medbox.medboxbackend.dispense_intervals.requests.CreateDispenseIntervalRequest;
import com.medbox.medboxbackend.dispense_intervals.requests.UpdateDispenseIntervalRequest;
import com.medbox.medboxbackend.model.DispenseInterval;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/dispense-intervals")
public class DispenseIntervalController {
    private final DispenseIntervalService dispenseIntervalService;

    public DispenseIntervalController(DispenseIntervalService dispenseIntervalService) {
        this.dispenseIntervalService = dispenseIntervalService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Create Dispense Interval", description = "Creates a new dispense interval for a compartment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispense interval created successfully"),
            @ApiResponse(responseCode = "404", description = "Compartment not found for the user"),
            @ApiResponse(responseCode = "400", description = "Invalid interval, pills to dispense, or start time")
    })
    public void createDispenseInterval(@RequestBody CreateDispenseIntervalRequest request,
                                                   Principal principal) {
        dispenseIntervalService.createDispenseInterval(
                request.compartmentId(),
                request.interval(),
                request.startTime(),
                request.pillsToDispense(),
                principal.getName()
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    @Operation(summary = "Update Dispense Interval", description = "Updates an existing dispense interval")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispense interval updated successfully"),
            @ApiResponse(responseCode = "404", description = "Dispense interval not found for the user"),
            @ApiResponse(responseCode = "400", description = "Invalid interval, pills to dispense, or start time")
    })
    public DispenseInterval updateDispenseInterval(@PathVariable Long id,
                                                   @RequestBody UpdateDispenseIntervalRequest request,
                                                   Principal principal) {
        return dispenseIntervalService.updateDispenseInterval(id, request.interval(), 
                request.startTime(), request.pillsToDispense(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Dispense Interval", description = "Deletes a dispense interval by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispense interval deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Dispense interval not found for the user")
    })
    public void deleteDispenseIntervalById(@PathVariable Long id, Principal principal) {
        dispenseIntervalService.deleteDispenseInterval(id, principal.getName());
    }
}

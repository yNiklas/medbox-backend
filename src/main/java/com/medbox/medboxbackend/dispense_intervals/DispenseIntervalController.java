package com.medbox.medboxbackend.dispense_intervals;

import com.medbox.medboxbackend.dispense_intervals.requests.UpdateDispenseIntervalRequest;
import com.medbox.medboxbackend.model.DispenseInterval;
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
    @PatchMapping("/{id}")
    public DispenseInterval updateDispenseInterval(@PathVariable Long id,
                                                   @RequestBody UpdateDispenseIntervalRequest request,
                                                   Principal principal) {
        return dispenseIntervalService.updateDispenseInterval(id, request.interval(), 
                request.startTime(), request.pillsToDispense(), principal.getName());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public void deleteDispenseIntervalById(@PathVariable Long id, Principal principal) {
        dispenseIntervalService.deleteDispenseInterval(id, principal.getName());
    }
}

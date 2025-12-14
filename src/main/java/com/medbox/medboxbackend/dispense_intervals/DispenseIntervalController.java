package com.medbox.medboxbackend.dispense_intervals;

import com.medbox.medboxbackend.model.DispenseInterval;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/dispense-intervals")
public class DispenseIntervalController {
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public DispenseInterval updateDispenseInterval(@RequestParam Long id,
                                                   @RequestBody DispenseInterval updatedInterval,
                                                   Principal principal) {
        // TODO: Implementation to update dispense interval for the authenticated user
        return null; // Placeholder return statement
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public void deleteDispenseIntervalById(@PathVariable Long id, Principal principal) {
        // TODO: Implementation to delete dispense interval by ID for the authenticated user
    }
}

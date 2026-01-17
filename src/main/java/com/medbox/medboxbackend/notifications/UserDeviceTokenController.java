package com.medbox.medboxbackend.notifications;

import com.medbox.medboxbackend.model.UserDeviceToken;
import com.medbox.medboxbackend.notifications.requests.RegisterDeviceTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/notifications")
public class UserDeviceTokenController {
    private final UserDeviceTokenService userDeviceTokenService;

    public UserDeviceTokenController(UserDeviceTokenService userDeviceTokenService) {
        this.userDeviceTokenService = userDeviceTokenService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/register-token")
    @Operation(summary = "Register device token", description = "Registers a device token for push notifications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device token registered successfully")
    })
    public UserDeviceToken registerDeviceToken(@RequestBody RegisterDeviceTokenRequest request, Principal principal) {
        return userDeviceTokenService.registerDeviceToken(
                principal.getName(),
                request.fcmToken(),
                request.deviceType()
        );
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/unregister-token")
    @Operation(summary = "Unregister device token", description = "Unregisters a device token for push notifications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device token unregistered successfully"),
            @ApiResponse(responseCode = "400", description = "Token does not belong to the user")
    })
    public void unregisterDeviceToken(@RequestParam String fcmToken, Principal principal) {
        userDeviceTokenService.unregisterDeviceToken(fcmToken, principal.getName());
    }
}

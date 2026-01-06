package com.medbox.medboxbackend.notifications;

import com.medbox.medboxbackend.model.UserDeviceToken;
import com.medbox.medboxbackend.notifications.requests.RegisterDeviceTokenRequest;
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
    public UserDeviceToken registerDeviceToken(@RequestBody RegisterDeviceTokenRequest request, Principal principal) {
        return userDeviceTokenService.registerDeviceToken(
                principal.getName(),
                request.fcmToken(),
                request.deviceType()
        );
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/unregister-token")
    public void unregisterDeviceToken(@RequestParam String fcmToken) {
        userDeviceTokenService.unregisterDeviceToken(fcmToken);
    }
}

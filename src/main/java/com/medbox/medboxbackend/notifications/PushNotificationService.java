package com.medbox.medboxbackend.notifications;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.medbox.medboxbackend.model.UserDeviceToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PushNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    private final UserDeviceTokenService userDeviceTokenService;

    public PushNotificationService(UserDeviceTokenService userDeviceTokenService) {
        this.userDeviceTokenService = userDeviceTokenService;
    }

    public void sendDispenseNotification(String userId, String boxName, int compartmentPosition, int pillsDispensed) {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase not initialized. Skipping notification for user: {}", userId);
            return;
        }

        List<UserDeviceToken> deviceTokens = userDeviceTokenService.getUserDeviceTokens(userId);
        
        if (deviceTokens.isEmpty()) {
            logger.debug("No device tokens found for user: {}", userId);
            return;
        }

        String title = "Pill Dispensed";
        String body = String.format("%d pill(s) dispensed from %s",
                                    pillsDispensed, boxName);

        List<String> invalidTokens = new ArrayList<>();

        for (UserDeviceToken deviceToken : deviceTokens) {
            try {
                Message message = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("boxName", boxName)
                        .putData("compartmentPosition", String.valueOf(compartmentPosition + 1))
                        .putData("pillsDispensed", String.valueOf(pillsDispensed))
                        .setToken(deviceToken.getFcmToken())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                logger.info("Successfully sent notification to user {}: {}", userId, response);
            } catch (FirebaseMessagingException e) {
                logger.error("Failed to send notification to user {}: {}", userId, e.getMessage());
                e.printStackTrace();
                
                // If the token is invalid or not registered, mark for removal
                if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                    e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    invalidTokens.add(deviceToken.getFcmToken());
                }
            }
        }

        // Clean up invalid tokens
        for (String invalidToken : invalidTokens) {
            try {
                userDeviceTokenService.unregisterInvalidToken(invalidToken);
                logger.info("Removed invalid token: {}", invalidToken);
            } catch (Exception e) {
                logger.error("Failed to remove invalid token {}: {}", invalidToken, e.getMessage());
            }
        }
    }
}

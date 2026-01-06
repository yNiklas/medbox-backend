package com.medbox.medboxbackend.notifications.requests;

public record RegisterDeviceTokenRequest(String fcmToken, String deviceType) {
}

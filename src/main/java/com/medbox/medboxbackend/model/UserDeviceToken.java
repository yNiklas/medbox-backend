package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class UserDeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Setter
    @Column(nullable = false, unique = true)
    private String fcmToken;

    @Setter
    @Column(nullable = false)
    private String deviceType; // "ANDROID" or "IOS"

    @Setter
    private long lastUpdated;

    public UserDeviceToken(String userId, String fcmToken, String deviceType) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.lastUpdated = System.currentTimeMillis();
    }
}

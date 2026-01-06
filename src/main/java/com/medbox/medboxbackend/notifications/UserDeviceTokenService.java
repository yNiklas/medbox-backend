package com.medbox.medboxbackend.notifications;

import com.medbox.medboxbackend.model.UserDeviceToken;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDeviceTokenService {
    private static final Logger logger = LoggerFactory.getLogger(UserDeviceTokenService.class);
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public UserDeviceTokenService(UserDeviceTokenRepository userDeviceTokenRepository) {
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    @Transactional
    public UserDeviceToken registerDeviceToken(String userId, String fcmToken, String deviceType) {
        // Check if token already exists
        Optional<UserDeviceToken> existingToken = userDeviceTokenRepository.findByFcmToken(fcmToken);
        
        if (existingToken.isPresent()) {
            // Update existing token
            UserDeviceToken token = existingToken.get();
            token.setDeviceType(deviceType);
            token.setLastUpdated(System.currentTimeMillis());
            logger.info("Updated device token for user: {}", userId);
            return userDeviceTokenRepository.save(token);
        } else {
            // Create new token
            UserDeviceToken token = new UserDeviceToken(userId, fcmToken, deviceType);
            logger.info("Registered new device token for user: {}", userId);
            return userDeviceTokenRepository.save(token);
        }
    }

    @Transactional
    public void unregisterDeviceToken(String fcmToken) {
        userDeviceTokenRepository.deleteByFcmToken(fcmToken);
        logger.info("Unregistered device token: {}", fcmToken);
    }

    public List<UserDeviceToken> getUserDeviceTokens(String userId) {
        return userDeviceTokenRepository.findAllByUserId(userId);
    }
}

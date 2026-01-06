package com.medbox.medboxbackend.notifications;

import com.medbox.medboxbackend.model.UserDeviceToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends CrudRepository<UserDeviceToken, Long> {
    List<UserDeviceToken> findAllByUserId(String userId);
    
    Optional<UserDeviceToken> findByFcmToken(String fcmToken);
    
    void deleteByFcmToken(String fcmToken);
}

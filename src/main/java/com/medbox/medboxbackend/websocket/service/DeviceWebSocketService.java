package com.medbox.medboxbackend.websocket.service;

import com.medbox.medboxbackend.websocket.dto.ChangeFunnelSpotRequest;
import tools.jackson.databind.ObjectMapper;
import com.medbox.medboxbackend.model.MedBox;
import com.medbox.medboxbackend.model.MedBoxStack;
import com.medbox.medboxbackend.notifications.PushNotificationService;
import com.medbox.medboxbackend.stacks.MedBoxStackRepository;
import com.medbox.medboxbackend.websocket.dto.DispenseRequest;
import com.medbox.medboxbackend.websocket.dto.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceWebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketService.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PushNotificationService pushNotificationService;
    private final MedBoxStackRepository medBoxStackRepository;

    public DeviceWebSocketService(PushNotificationService pushNotificationService, 
                                   MedBoxStackRepository medBoxStackRepository) {
        this.pushNotificationService = pushNotificationService;
        this.medBoxStackRepository = medBoxStackRepository;
    }

    public void registerSession(String mosMac, WebSocketSession session) {
        sessions.put(mosMac, session);
        logger.info("Device {} connected", mosMac);
    }

    public void removeSession(String mosMac) {
        sessions.remove(mosMac);
        logger.info("Device {} disconnected", mosMac);
    }

    public boolean isDeviceConnected(String mosMac) {
        return sessions.containsKey(mosMac);
    }

    public void requestDispense(String mosMac, String boxMac, int compartmentNumber, int amountOfPills) throws IOException {
        WebSocketSession session = sessions.get(mosMac);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Device " + mosMac + " is not connected");
        }

        DispenseRequest dispenseRequest = new DispenseRequest(boxMac, compartmentNumber, amountOfPills);
        ServerMessage message = new ServerMessage(3, dispenseRequest);
        
        String jsonMessage = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(jsonMessage));
        logger.info("Sent dispense request to device {}: boxMac={}, compartment={}, pills={}", 
                    mosMac, boxMac, compartmentNumber, amountOfPills);

        // Send push notification
        sendDispenseNotification(mosMac, boxMac, compartmentNumber, amountOfPills);
    }

    private void sendDispenseNotification(String mosMac, String boxMac, int compartmentNumber, int amountOfPills) {
        try {
            Optional<MedBoxStack> stackOpt = medBoxStackRepository.findMedBoxStackByMedBoxMACAddress(mosMac);
            if (stackOpt.isEmpty()) {
                logger.warn("Stack not found for mosMac: {}", mosMac);
                return;
            }

            MedBoxStack stack = stackOpt.get();
            String userId = stack.getUserId();
            
            // Find the box by MAC address
            Optional<MedBox> boxOpt = stack.getBoxes().stream()
                    .filter(box -> box.getMac().equals(boxMac))
                    .findFirst();
            
            String boxName = boxOpt.map(MedBox::getName).orElse("Unknown Box");

            pushNotificationService.sendDispenseNotification(userId, boxName, compartmentNumber, amountOfPills);
        } catch (Exception e) {
            logger.error("Failed to send dispense notification: {}", e.getMessage(), e);
        }
    }

    public void requestFunnelSpotChange(String mosMac, String targetBoxMAC, int targetCompartmentNumber) throws IOException {
        WebSocketSession session = sessions.get(mosMac);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Device " + mosMac + " is not connected");
        }

        ChangeFunnelSpotRequest changeFunnelSpotRequest = new ChangeFunnelSpotRequest(targetBoxMAC, targetCompartmentNumber);
        ServerMessage message = new ServerMessage(4, changeFunnelSpotRequest);

        String jsonMessage = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(jsonMessage));
        logger.info("Sent funnel spot change request to device {}: targetBoxMAC={}, targetCompartmentNumber={}",
                    mosMac, targetBoxMAC, targetCompartmentNumber);
    }
}

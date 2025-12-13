package com.medbox.medboxbackend.websocket.service;

import tools.jackson.databind.ObjectMapper;
import com.medbox.medboxbackend.websocket.dto.DispenseRequest;
import com.medbox.medboxbackend.websocket.dto.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceWebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketService.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void registerSession(String deviceId, WebSocketSession session) {
        sessions.put(deviceId, session);
        logger.info("Device {} connected", deviceId);
    }

    public void removeSession(String deviceId) {
        sessions.remove(deviceId);
        logger.info("Device {} disconnected", deviceId);
    }

    public boolean isDeviceConnected(String deviceId) {
        return sessions.containsKey(deviceId);
    }

    public void requestDispense(String id, String boxMac, int compartmentNumber, int amountOfPills) throws IOException {
        WebSocketSession session = sessions.get(id);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Device " + id + " is not connected");
        }

        DispenseRequest dispenseRequest = new DispenseRequest(boxMac, compartmentNumber, amountOfPills);
        ServerMessage message = new ServerMessage(3, dispenseRequest);
        
        String jsonMessage = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(jsonMessage));
        logger.info("Sent dispense request to device {}: boxMac={}, compartment={}, pills={}", 
                    id, boxMac, compartmentNumber, amountOfPills);
    }
}

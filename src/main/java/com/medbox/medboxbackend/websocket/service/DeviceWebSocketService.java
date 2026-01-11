package com.medbox.medboxbackend.websocket.service;

import com.medbox.medboxbackend.websocket.dto.ChangeFunnelSpotRequest;
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

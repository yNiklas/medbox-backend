package com.medbox.medboxbackend.websocket.handler;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.medbox.medboxbackend.websocket.dto.*;
import com.medbox.medboxbackend.websocket.service.DeviceWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketHandler.class);
    private final DeviceWebSocketService deviceWebSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceWebSocketHandler(DeviceWebSocketService deviceWebSocketService) {
        this.deviceWebSocketService = deviceWebSocketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceId = extractDeviceId(session);
        if (deviceId != null) {
            deviceWebSocketService.registerSession(deviceId, session);
        } else {
            logger.warn("Connection attempted without device ID");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String deviceId = extractDeviceId(session);
        if (deviceId == null) {
            logger.warn("Received message from unidentified device");
            return;
        }

        try {
            ClientMessage clientMessage = objectMapper.readValue(message.getPayload(), ClientMessage.class);
            handleClientMessage(deviceId, clientMessage);
        } catch (Exception e) {
            logger.error("Error parsing message from device {}: {}", deviceId, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String deviceId = extractDeviceId(session);
        if (deviceId != null) {
            deviceWebSocketService.removeSession(deviceId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String deviceId = extractDeviceId(session);
        logger.error("Transport error for device {}: {}", deviceId, exception.getMessage());
    }

    private String extractDeviceId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    private void handleClientMessage(String deviceId, ClientMessage clientMessage) throws IOException {
        int messageType = clientMessage.getMessageType();
        Object messageContent = clientMessage.getMessage();

        switch (messageType) {
            case 0: // Topology information
                handleTopologyMessage(deviceId, messageContent);
                break;
            case 1: // Keepalive
                handleKeepaliveMessage(deviceId, messageContent);
                break;
            case 2: // Error
                handleErrorMessage(deviceId, messageContent);
                break;
            default:
                logger.warn("Unknown message type {} from device {}", messageType, deviceId);
        }
    }

    private void handleTopologyMessage(String deviceId, Object messageContent) throws IOException {
        Map<String, String> topology = objectMapper.convertValue(messageContent, new TypeReference<Map<String, String>>() {});
        logger.info("Received topology from device {}: {}", deviceId, topology);
    }

    private void handleKeepaliveMessage(String deviceId, Object messageContent) throws IOException {
        KeepaliveMessage keepalive = objectMapper.convertValue(messageContent, KeepaliveMessage.class);
        logger.debug("Received keepalive from device {}: status={}", deviceId, keepalive.getStatus());
    }

    private void handleErrorMessage(String deviceId, Object messageContent) throws IOException {
        ErrorMessage error = objectMapper.convertValue(messageContent, ErrorMessage.class);
        logger.error("Received error from device {}: error={}, content={}", 
                     deviceId, error.getError(), error.getContent());
    }
}

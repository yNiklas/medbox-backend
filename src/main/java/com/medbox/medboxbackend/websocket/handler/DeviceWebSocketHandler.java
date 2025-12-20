package com.medbox.medboxbackend.websocket.handler;

import com.medbox.medboxbackend.boxes.MedBoxDispenseSchedulerService;
import com.medbox.medboxbackend.boxes.MedBoxService;
import com.medbox.medboxbackend.stacks.MedBoxStackService;
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
import java.util.List;
import java.util.Map;

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketHandler.class);
    private final DeviceWebSocketService deviceWebSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MedBoxStackService medBoxStackService;
    private final MedBoxDispenseSchedulerService medBoxDispenseSchedulerService;

    public DeviceWebSocketHandler(DeviceWebSocketService deviceWebSocketService, MedBoxStackService medBoxStackService, MedBoxDispenseSchedulerService medBoxDispenseSchedulerService) {
        this.deviceWebSocketService = deviceWebSocketService;
        this.medBoxStackService = medBoxStackService;
        this.medBoxDispenseSchedulerService = medBoxDispenseSchedulerService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceMAC = extractDeviceMAC(session);
        if (deviceMAC != null) {
            deviceWebSocketService.registerSession(deviceMAC, session);
            medBoxDispenseSchedulerService.reScheduleStack(deviceMAC);
        } else {
            logger.warn("Connection attempted without device MAC");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String deviceMAC = extractDeviceMAC(session);
        if (deviceMAC == null) {
            logger.warn("Received message from unidentified device");
            return;
        }

        try {
            ClientMessage clientMessage = objectMapper.readValue(message.getPayload(), ClientMessage.class);
            handleClientMessage(deviceMAC, clientMessage);
        } catch (Exception e) {
            logger.error("Error parsing message from device {}: {}", deviceMAC, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String deviceMAC = extractDeviceMAC(session);
        if (deviceMAC != null) {
            deviceWebSocketService.removeSession(deviceMAC);
            medBoxDispenseSchedulerService.removeScheduledDispense(deviceMAC);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String deviceMAC = extractDeviceMAC(session);
        logger.error("Transport error for device {}: {}", deviceMAC, exception.getMessage());
    }

    private String extractDeviceMAC(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    private void handleClientMessage(String deviceMAC, ClientMessage clientMessage) throws IOException {
        int messageType = clientMessage.getMessageType();
        Object messageContent = clientMessage.getMessage();

        switch (messageType) {
            case 0: // Topology information
                handleTopologyMessage(deviceMAC, messageContent);
                break;
            case 1: // Keepalive
                handleKeepaliveMessage(deviceMAC, messageContent);
                break;
            case 2: // Error
                handleErrorMessage(deviceMAC, messageContent);
                break;
            default:
                logger.warn("Unknown message type {} from device {}", messageType, deviceMAC);
        }
    }

    private void handleTopologyMessage(String deviceMAC, Object messageContent) throws IOException {
        Map<String, String> topology = objectMapper.convertValue(messageContent, new TypeReference<Map<String, String>>() {});
        logger.info("Received topology from device {}: {}", deviceMAC, topology);
        try {
            List<String> macsInPhysicalOrder = topology.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList();
            medBoxStackService.registerStackOrder(deviceMAC, macsInPhysicalOrder);
        } catch (IllegalArgumentException e) {
            logger.error("Received topology, but failed to register stack order for stack with master device MAC {}: {}", deviceMAC, e.getMessage());
        }
    }

    private void handleKeepaliveMessage(String deviceMAC, Object messageContent) throws IOException {
        KeepaliveMessage keepalive = objectMapper.convertValue(messageContent, KeepaliveMessage.class);
        logger.debug("Received keepalive from device {}: status={}", deviceMAC, keepalive.getStatus());
        try {
            medBoxStackService.updateOnlineStatusOfStackByMasterMAC(deviceMAC);
        } catch (IllegalArgumentException e) {
            logger.error("Received keepalive, but failed to update online status for stack with master device MAC {}: {}", deviceMAC, e.getMessage());
        }
    }

    private void handleErrorMessage(String deviceMAC, Object messageContent) throws IOException {
        ErrorMessage error = objectMapper.convertValue(messageContent, ErrorMessage.class);
        logger.error("Received error from device {}: error={}, content={}", 
                     deviceMAC, error.getError(), error.getContent());
        try {
            medBoxStackService.handleStackError(error.getError(), error.getContent());
        } catch (IllegalArgumentException e) {
            logger.error("Received error, but failed to handle error for device of stack with MOS {}: {}", deviceMAC, e.getMessage());
        }
    }
}

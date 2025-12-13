package com.medbox.medboxbackend.websocket;

import com.medbox.medboxbackend.MedBoxBackendApplication;
import com.medbox.medboxbackend.websocket.dto.*;
import com.medbox.medboxbackend.websocket.service.DeviceWebSocketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {MedBoxBackendApplication.class, TestSecurityConfig.class})
class DeviceWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DeviceWebSocketService deviceWebSocketService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDeviceConnection() throws Exception {
        String deviceId = "test-device-1";
        String wsUrl = "ws://localhost:" + port + "/device/" + deviceId;

        CountDownLatch connectionLatch = new CountDownLatch(1);
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                connectionLatch.countDown();
            }
        }, wsUrl).get(5, TimeUnit.SECONDS);

        assertTrue(connectionLatch.await(2, TimeUnit.SECONDS));
        assertTrue(deviceWebSocketService.isDeviceConnected(deviceId));
        
        session.close();
        Thread.sleep(500); // Wait for cleanup
        assertFalse(deviceWebSocketService.isDeviceConnected(deviceId));
    }

    @Test
    void testTopologyMessage() throws Exception {
        String deviceId = "test-device-2";
        String wsUrl = "ws://localhost:" + port + "/device/" + deviceId;

        CountDownLatch connectionLatch = new CountDownLatch(1);
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                connectionLatch.countDown();
                
                // Send topology message
                Map<String, String> topology = new HashMap<>();
                topology.put("0", "AA:BB:CC:DD:EE:01");
                topology.put("1", "AA:BB:CC:DD:EE:02");
                
                ClientMessage message = new ClientMessage();
                message.setMessageType(0);
                message.setMessage(topology);
                
                String json = new ObjectMapper().writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        }, wsUrl).get(5, TimeUnit.SECONDS);

        assertTrue(connectionLatch.await(2, TimeUnit.SECONDS));
        Thread.sleep(500); // Wait for message processing
        
        session.close();
    }

    @Test
    void testKeepaliveMessage() throws Exception {
        String deviceId = "test-device-3";
        String wsUrl = "ws://localhost:" + port + "/device/" + deviceId;

        CountDownLatch connectionLatch = new CountDownLatch(1);
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                connectionLatch.countDown();
                
                // Send keepalive message
                Map<String, String> keepalive = new HashMap<>();
                keepalive.put("status", "online");
                
                ClientMessage message = new ClientMessage();
                message.setMessageType(1);
                message.setMessage(keepalive);
                
                String json = new ObjectMapper().writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        }, wsUrl).get(5, TimeUnit.SECONDS);

        assertTrue(connectionLatch.await(2, TimeUnit.SECONDS));
        Thread.sleep(500); // Wait for message processing
        
        session.close();
    }

    @Test
    void testErrorMessage() throws Exception {
        String deviceId = "test-device-4";
        String wsUrl = "ws://localhost:" + port + "/device/" + deviceId;

        CountDownLatch connectionLatch = new CountDownLatch(1);
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                connectionLatch.countDown();
                
                // Send error message
                Map<String, Object> error = new HashMap<>();
                error.put("error", 404);
                error.put("content", "Box not found");
                
                ClientMessage message = new ClientMessage();
                message.setMessageType(2);
                message.setMessage(error);
                
                String json = new ObjectMapper().writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        }, wsUrl).get(5, TimeUnit.SECONDS);

        assertTrue(connectionLatch.await(2, TimeUnit.SECONDS));
        Thread.sleep(500); // Wait for message processing
        
        session.close();
    }

    @Test
    void testRequestDispense() throws Exception {
        String deviceId = "test-device-5";
        String wsUrl = "ws://localhost:" + port + "/device/" + deviceId;

        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<String> receivedMessage = new AtomicReference<>();
        
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                connectionLatch.countDown();
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                receivedMessage.set(message.getPayload());
                messageLatch.countDown();
            }
        }, wsUrl).get(5, TimeUnit.SECONDS);

        assertTrue(connectionLatch.await(2, TimeUnit.SECONDS));
        
        // Send dispense request from server
        deviceWebSocketService.requestDispense(deviceId, "AA:BB:CC:DD:EE:01", 2, 3);
        
        assertTrue(messageLatch.await(2, TimeUnit.SECONDS));
        
        // Verify the received message
        String json = receivedMessage.get();
        assertNotNull(json);
        
        ServerMessage serverMessage = objectMapper.readValue(json, ServerMessage.class);
        assertEquals(3, serverMessage.getMessageType());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> dispenseData = objectMapper.convertValue(serverMessage.getMessage(), Map.class);
        assertEquals("AA:BB:CC:DD:EE:01", dispenseData.get("targetBoxMAC"));
        assertEquals(2, dispenseData.get("compartmentNumber"));
        assertEquals(3, dispenseData.get("amountOfPillsToDispense"));
        
        session.close();
    }

    @Test
    void testRequestDispenseToDisconnectedDevice() {
        String deviceId = "non-existent-device";
        
        assertThrows(IllegalStateException.class, () -> {
            deviceWebSocketService.requestDispense(deviceId, "AA:BB:CC:DD:EE:01", 1, 2);
        });
    }
}

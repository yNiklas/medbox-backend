package com.medbox.medboxbackend.robots;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RobotWebSocketClient extends WebSocketClient {
    private final String robotName;
    private final ObjectMapper objectMapper;

    public RobotWebSocketClient(URI serverUri, String robotName) {
        super(serverUri);
        this.robotName = robotName;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to robot: " + robotName);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Message from " + robotName + ": " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from robot: " + robotName + " (code: " + code + ", reason: " + reason + ")");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Error in robot connection " + robotName + ": " + ex.getMessage());
    }

    public void sendMessage(RobotService.PoseStamped pose) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("op", "publish");
            message.put("topic", "/goal_pose");
            message.put("msg", pose);

            String jsonMessage = objectMapper.writeValueAsString(message);
            this.send(jsonMessage);
            System.out.println("Sent goal to " + robotName + ": x=" + pose.pose.position.x + 
                    ", y=" + pose.pose.position.y + ", z=" + pose.pose.position.z);
        } catch (Exception e) {
            System.err.println("Error sending message to " + robotName + ": " + e.getMessage());
        }
    }
}

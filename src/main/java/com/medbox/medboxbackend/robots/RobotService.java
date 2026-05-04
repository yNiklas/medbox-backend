package com.medbox.medboxbackend.robots;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RobotService {
    private static final double PROXIMITY_THRESHOLD = 2.0; // meters
    private static final long ROBOT_ARRIVAL_TIMEOUT = 600_000; // 10 minutes in milliseconds
    
    private final Map<String, String> robotWebsockets;
    private final Map<String, RobotWebSocketClient> activeConnections;
    private final Map<String, RobotPoseWithCovariance> robotCurrentPoses;

    public RobotService() {
        this.robotWebsockets = loadRobotsFromJson();
        this.activeConnections = new ConcurrentHashMap<>();
        this.robotCurrentPoses = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void connectToRobots() {
        for (Map.Entry<String, String> robot : robotWebsockets.entrySet()) {
            String robotName = robot.getKey();
            String websocketUrl = robot.getValue();
            try {
                connectToRobot(robotName, websocketUrl);
            } catch (Exception e) {
                System.err.println("Failed to connect to robot " + robotName + " at " + websocketUrl + ": " + e.getMessage());
            }
        }
    }

    private void connectToRobot(String robotName, String websocketUrl) throws Exception {
        RobotWebSocketClient client = new RobotWebSocketClient(new URI(websocketUrl), robotName);
        client.setPoseCallback(pose -> robotCurrentPoses.put(robotName, pose));
        client.connect();
        activeConnections.put(robotName, client);
    }

    @PreDestroy
    public void disconnectAll() {
        for (RobotWebSocketClient client : activeConnections.values()) {
            try {
                client.close();
            } catch (Exception e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public void sendGoal(String robotName, double x, double y, double z, double yaw) {
        RobotWebSocketClient client = activeConnections.get(robotName);
        if (client == null || !client.isOpen()) {
            throw new IllegalArgumentException("Robot '" + robotName + "' is not connected");
        }

        PoseStamped goal = createPoseStamped(x, y, z, yaw);
        client.sendMessage(goal);
    }

    public boolean waitForRobotAtLocation(String robotName, double targetX, double targetY, double targetZ) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeout = ROBOT_ARRIVAL_TIMEOUT;
        
        while (System.currentTimeMillis() - startTime < timeout) {
            RobotPoseWithCovariance currentPose = robotCurrentPoses.get(robotName);
            if (currentPose != null && isRobotAtLocation(currentPose, targetX, targetY, targetZ)) {
                System.out.println("Robot " + robotName + " arrived at location (" + targetX + ", " + targetY + ", " + targetZ + ")");
                return true;
            }
            Thread.sleep(100); // Check every 100ms
        }
        
        System.err.println("Robot " + robotName + " did not arrive at location within timeout");
        return false;
    }

    private boolean isRobotAtLocation(RobotPoseWithCovariance pose, double targetX, double targetY, double targetZ) {
        if (pose == null || pose.pose == null || pose.pose.pose == null || pose.pose.pose.position == null) {
            return false;
        }
        
        RobotPoseWithCovariance.Position robotPos = pose.pose.pose.position;
        RobotPoseWithCovariance.Position targetPos = new RobotPoseWithCovariance.Position(targetX, targetY, targetZ);
        double distance = robotPos.distanceTo(targetPos);
        
        return distance <= PROXIMITY_THRESHOLD;
    }

    private PoseStamped createPoseStamped(double x, double y, double z, double yaw) {
        PoseStamped pose = new PoseStamped();
        pose.header = new Header();
        pose.header.seq = 0;
        pose.header.stamp = new Timestamp();
        pose.header.stamp.secs = (int) (System.currentTimeMillis() / 1000);
        pose.header.stamp.nsecs = (int) ((System.currentTimeMillis() % 1000) * 1_000_000);
        pose.header.frame_id = "map";

        pose.pose = new Pose();
        pose.pose.position = new Position(x, y, z);
        pose.pose.orientation = quaternionFromYaw(yaw);

        return pose;
    }

    private Quaternion quaternionFromYaw(double yaw) {
        double halfYaw = yaw / 2.0;
        Quaternion q = new Quaternion();
        q.x = 0.0;
        q.y = 0.0;
        q.z = Math.sin(halfYaw);
        q.w = Math.cos(halfYaw);
        return q;
    }

    private Map<String, String> loadRobotsFromJson() {
        Map<String, String> result = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("robots.json");
            if (inputStream != null) {
                Map<String, String> data = objectMapper.readValue(inputStream, Map.class);
                result.putAll(data);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load robots.json", e);
        }
        return result;
    }

    public List<String> getAvailableRobots() {
        return new ArrayList<>(activeConnections.keySet());
    }

    public List<String> getAllConfiguredRobots() {
        return new ArrayList<>(robotWebsockets.keySet());
    }

    public boolean isValidRobot(String robotName) {
        return robotName != null && robotWebsockets.containsKey(robotName);
    }

    public boolean isRobotConnected(String robotName) {
        RobotWebSocketClient client = activeConnections.get(robotName);
        return client != null && client.isOpen();
    }

    public static class PoseStamped {
        public Header header;
        public Pose pose;
    }

    public static class Header {
        public int seq;
        public Timestamp stamp;
        public String frame_id;
    }

    public static class Timestamp {
        public int secs;
        public int nsecs;
    }

    public static class Pose {
        public Position position;
        public Quaternion orientation;
    }

    public static class Position {
        public double x;
        public double y;
        public double z;

        public Position() {}

        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Quaternion {
        public double x;
        public double y;
        public double z;
        public double w;
    }
}

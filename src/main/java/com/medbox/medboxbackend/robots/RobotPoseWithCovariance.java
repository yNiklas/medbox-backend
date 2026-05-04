package com.medbox.medboxbackend.robots;

public class RobotPoseWithCovariance {
    public Header header;
    public PoseWithCovariance pose;

    public static class Header {
        public int seq;
        public Timestamp stamp;
        public String frame_id;
    }

    public static class Timestamp {
        public int secs;
        public int nsecs;
    }

    public static class PoseWithCovariance {
        public Pose pose;
        public double[] covariance;
    }

    public static class Pose {
        public Position position;
        public Orientation orientation;
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

        public double distanceTo(Position other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double dz = this.z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    public static class Orientation {
        public double x;
        public double y;
        public double z;
        public double w;
    }
}

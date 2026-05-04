package com.medbox.medboxbackend.robots;

public interface RobotPoseCallback {
    void onPoseUpdate(RobotPoseWithCovariance pose);
}

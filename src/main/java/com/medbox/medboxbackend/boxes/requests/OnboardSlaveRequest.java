package com.medbox.medboxbackend.boxes.requests;

public record OnboardSlaveRequest(
        String boxName,
        String boxMac
) {
}

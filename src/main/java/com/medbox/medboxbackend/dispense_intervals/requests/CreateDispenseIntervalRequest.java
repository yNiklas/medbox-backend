package com.medbox.medboxbackend.dispense_intervals.requests;

public record CreateDispenseIntervalRequest(
        Long compartmentId,
        long interval,
        long startTime,
        int pillsToDispense
) {
}

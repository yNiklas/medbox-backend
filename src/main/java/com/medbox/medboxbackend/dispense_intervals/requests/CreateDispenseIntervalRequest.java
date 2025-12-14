package com.medbox.medboxbackend.dispense_intervals.requests;

public record CreateDispenseIntervalRequest(
        long interval,
        long startTime,
        int pillsToDispense
) {
}

package com.medbox.medboxbackend.dispense_intervals.requests;

public record UpdateDispenseIntervalRequest(
        long interval,
        long startTime,
        int pillsToDispense
) {
}

package com.medbox.medboxbackend.stacks.requests;

public record AssignMedBoxStackRequest(
    String masterMACAddress,
    String boxName,
    String stackName
) {
}

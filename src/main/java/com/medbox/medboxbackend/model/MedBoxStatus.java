package com.medbox.medboxbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class MedBoxStatus {
    @Id
    private String id;

    private Long lastSeenAt; // UTC timestamp in milliseconds
    private String error; // potential error message
}

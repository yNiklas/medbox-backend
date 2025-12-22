package com.medbox.medboxbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
public class MedBoxStatus {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Setter
    private Long lastSeenAt; // UTC timestamp in milliseconds

    @Setter
    private String error; // potential error message

    public MedBoxStatus(Long lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}

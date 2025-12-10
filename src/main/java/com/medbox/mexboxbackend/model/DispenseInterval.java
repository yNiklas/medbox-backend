package com.medbox.mexboxbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class DispenseInterval {
    @Id
    private String id;

    private Long interval;
    private Long startTime;
}

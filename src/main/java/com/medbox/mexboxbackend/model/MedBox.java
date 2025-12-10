package com.medbox.mexboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class MedBox {
    @Id
    private String id;

    private String mac;
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MedBoxStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DispenseSchedule> compartments;
}

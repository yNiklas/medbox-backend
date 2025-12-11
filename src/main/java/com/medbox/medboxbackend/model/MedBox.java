package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MedBox {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private String id;

    private String mac;
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MedBoxStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DispenseSchedule> compartments;

    public MedBox(String mac, String name) {
        this.mac = mac;
        this.name = name;
        this.compartments = List.of(
                new DispenseSchedule("Compartment 1"),
                new DispenseSchedule("Compartment 2"),
                new DispenseSchedule("Compartment 3"),
                new DispenseSchedule("Compartment 4")
        );
    }
}

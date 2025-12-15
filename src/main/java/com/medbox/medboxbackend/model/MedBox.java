package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MedBox {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String mac;

    @Setter
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MedBoxStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compartment> compartments;

    public MedBox(String mac, String name) {
        this.mac = mac;
        this.name = name;
        this.compartments = List.of(
                new Compartment("Compartment 1"),
                new Compartment("Compartment 2"),
                new Compartment("Compartment 3"),
                new Compartment("Compartment 4")
        );
    }

    public void updateOnlineStatus() {
        if (status == null) {
            status = new MedBoxStatus();
        }
        status.setLastSeenAt(System.currentTimeMillis());
        status.setError(null);
    }

    public void registerError(String error) {
        if (status == null) {
            status = new MedBoxStatus();
        }
        status.setError(error);
    }
}

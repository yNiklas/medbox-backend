package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

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
        this.status = new MedBoxStatus(System.currentTimeMillis());
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

    public void onDisconnected() {
        if (status == null) {
            status = new MedBoxStatus();
        }
        status.setError("Device disconnected");
    }

    public Optional<Integer> getCompartmentByDispenseIntervalId(Long dispenseIntervalId) {
        for (int i = 0; i < compartments.size(); i++) {
            Compartment compartment = compartments.get(i);
            if (compartment.hasIntervalId(dispenseIntervalId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public boolean hasCompartment(Long compartmentId) {
        return compartments != null && compartments.stream().anyMatch(c -> c.getId().equals(compartmentId));
    }

    public Optional<Integer> getCompartmentNumberById(Long compartmentId) {
        for (int i = 0; i < compartments.size(); i++) {
            Compartment compartment = compartments.get(i);
            if (compartment.getId().equals(compartmentId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }
}

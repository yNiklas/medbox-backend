package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor
public class MedBoxStack {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedBox> boxes;

    boolean orderChanged;

    @ElementCollection
    @MapKeyColumn(name = "mac")
    @Column(name = "position")
    @CollectionTable(name = "medboxstack_dangling_macs", joinColumns = @JoinColumn(name = "med_box_stack_id"))
    private Map<String, Integer> danglingMACs = new HashMap<>();

    private String userId;

    public MedBoxStack(String name, MedBox masterMedBox, String userId) {
        this.name = name;
        this.boxes = List.of(masterMedBox);
        this.userId = userId;
    }

    public void updateOrder(List<String> macsInPhysicalOrder) {
        if (macsInPhysicalOrder == null) return;

        if (boxes != null) {
            for (int i = 0; i < macsInPhysicalOrder.size(); i++) {
                String mac = macsInPhysicalOrder.get(i);
                if (boxes.size() <= i) {
                    danglingMACs.put(mac, i);
                } else if (!Objects.equals(boxes.get(i).getMac(), mac)) {
                    orderChanged = true;
                    Optional<MedBox> existingBox = boxes.stream().filter(eb -> Objects.equals(eb.getMac(), mac)).findFirst();
                    if (existingBox.isEmpty()) {
                        danglingMACs.put(mac, i);
                    } else {
                        // Reorder
                        boxes.remove(existingBox.get());
                        boxes.add(i, existingBox.get());
                    }
                }
            }
        }
    }

    public MedBox onboardSlaveMedBox(String slaveBoxMac, String slaveBoxName) {
        if (danglingMACs == null || !danglingMACs.containsKey(slaveBoxMac)) {
            throw new IllegalArgumentException("No dangling MAC address found for " + slaveBoxMac);
        }

        int position = danglingMACs.get(slaveBoxMac);
        MedBox slaveBox = new MedBox(slaveBoxMac, slaveBoxName);
        boxes.add(position, slaveBox);
        danglingMACs.remove(slaveBoxMac);
        return slaveBox;
    }

    @JsonIgnore
    public Optional<MedBox> getMos() {
        if (boxes == null || boxes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(boxes.getFirst());
    }

    public Optional<MedBox> findMedBoxByDispenseIntervalId(Long dispenseIntervalId) {
        if (boxes == null) {
            return Optional.empty();
        }
        for (MedBox box : boxes) {
            for (Compartment compartment : box.getCompartments()) {
                if (compartment.hasIntervalId(dispenseIntervalId)) {
                    return Optional.of(box);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<MedBox> findMedBoxByCompartmentId(Long compartmentId) {
        if (boxes == null) {
            return Optional.empty();
        }
        for (MedBox box : boxes) {
            if (box.hasCompartment(compartmentId)) {
                return Optional.of(box);
            }
        }
        return Optional.empty();
    }
}

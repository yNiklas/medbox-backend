package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @MapKeyColumn(name = "position")
    @Column(name = "mac")
    @CollectionTable(name = "medboxstack_dangling_macs", joinColumns = @JoinColumn(name = "med_box_stack_id"))
    private Map<Integer, String> danglingMACs;

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
                    danglingMACs.put(i, mac);
                } else if (!Objects.equals(boxes.get(i).getMac(), mac)) {
                    orderChanged = true;
                    boolean isDangling = boxes.stream().anyMatch(existingBox -> Objects.equals(existingBox.getMac(), mac));
                    if (isDangling) danglingMACs.put(i, mac);
                }
            }
        }
    }
}

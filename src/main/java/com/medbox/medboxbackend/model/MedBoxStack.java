package com.medbox.medboxbackend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MedBoxStack {
    @Id
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedBox> boxes;

    private String userId;

    public MedBoxStack(String name, MedBox masterMedBox, String userId) {
        this.name = name;
        this.boxes = List.of(masterMedBox);
        this.userId = userId;
    }
}

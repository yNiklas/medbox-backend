package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MedBoxStack {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
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

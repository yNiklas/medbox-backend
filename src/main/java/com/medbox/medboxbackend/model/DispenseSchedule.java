package com.medbox.medboxbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class DispenseSchedule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DispenseInterval> intervals;

    public DispenseSchedule(String name) {
        this.name = name;
    }
}

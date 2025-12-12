package com.medbox.medboxbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor
public class Compartment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DispenseInterval> intervals;

    private int remainingPills = 0;

    private long lastDispenseTime; // UTC timestamp in milliseconds

    public Compartment(String name) {
        this.name = name;
        this.intervals = new ArrayList<>(1);
    }

    public boolean isRunningOut() {
        return remainingPills <= 5; // todo: Replace with intelligent threshold
    }

    public String getPotentialErrorMessage() {
        Optional<DispenseInterval> nextIntervalWithMissingPills = getNextDispenseIntervalWithMissingPills();
        if (nextIntervalWithMissingPills.isPresent()) {
            DispenseInterval interval = nextIntervalWithMissingPills.get();
            long nextDispenseTime = interval.getNextDispenseTime();
            int pillsNeeded = interval.getPillsToDispense() - remainingPills;
            return String.format("Not enough pills! Refill at least %d more pills until %tc", pillsNeeded, nextDispenseTime);
        } else {
            return null;
        }
    }

    @JsonIgnore
    public Optional<Long> getNextDispenseTime() {
        if (intervals == null) return Optional.empty();
        long now = System.currentTimeMillis();
        return intervals.stream()
                .filter(DispenseInterval::hasUpcomingDispense)
                .map(interval -> interval.getNextDispenseTime(now))
                .min(Long::compareTo);
    }

    @JsonIgnore
    private Optional<DispenseInterval> getNextDispenseIntervalWithMissingPills() {
        if (intervals == null) return Optional.empty();
        long now = System.currentTimeMillis();
        return intervals.stream()
                .filter(interval -> interval.hasUpcomingDispense() && interval.getPillsToDispense() > remainingPills)
                .min(Comparator.comparingLong((DispenseInterval di) -> di.getNextDispenseTime(now)));
    }
}

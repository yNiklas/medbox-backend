package com.medbox.medboxbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DispenseInterval> intervals;

    @Setter
    private int remainingPills = 0;

    private long lastDispenseTime; // UTC timestamp in milliseconds

    private int position; // Position index in the actual box

    public Compartment(String name, int position) {
        this.name = name;
        this.intervals = new ArrayList<>(1);
        this.position = position;
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
    public int intervalCount() {
        return intervals == null ? 0 : intervals.size();
    }

    public void appendInterval(DispenseInterval interval) {
        if (intervals == null) {
            intervals = new ArrayList<>();
        }
        intervals.add(interval);
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

    public boolean hasIntervalId(Long intervalId) {
        if (intervals == null) return false;
        return intervals.stream().anyMatch(interval -> interval.getId().equals(intervalId));
    }

    public void removePills(int amount) {
        this.remainingPills -= amount;
        if (this.remainingPills < 0) {
            this.remainingPills = 0;
        }
        this.lastDispenseTime = System.currentTimeMillis();
    }
}

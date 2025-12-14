package com.medbox.medboxbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class DispenseInterval {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Setter
    private long interval;
    @Setter
    private long startTime;
    @Setter
    private int pillsToDispense;

    @JsonIgnore
    public boolean hasUpcomingDispense() {
        return interval >= 0;
    }

    @JsonIgnore
    public long getNextDispenseTime(long evaluationTime) {
        if (interval < 0) {
            return -1;
        }
        long elapsedSinceStart = evaluationTime - startTime;
        if (elapsedSinceStart < 0) {
            return startTime;
        }
        long intervalsPassed = elapsedSinceStart / interval;
        return startTime + (intervalsPassed + 1) * interval;
    }

    @JsonIgnore
    public long getNextDispenseTime() {
        return getNextDispenseTime(System.currentTimeMillis());
    }
}

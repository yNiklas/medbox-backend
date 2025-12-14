package com.medbox.medboxbackend.dispense_intervals;

import com.medbox.medboxbackend.model.DispenseInterval;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispenseIntervalRepository extends CrudRepository<DispenseInterval, Long> {
    @Query("SELECT di FROM DispenseInterval di JOIN Compartment c ON di MEMBER OF c.intervals JOIN MedBox mb ON c MEMBER OF mb.compartments JOIN MedBoxStack mbs ON mb MEMBER OF mbs.boxes WHERE di.id = :id AND mbs.userId = :userId")
    Optional<DispenseInterval> findByIdAndUserId(Long id, String userId);
}

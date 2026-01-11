package com.medbox.medboxbackend.compartments;

import com.medbox.medboxbackend.model.Compartment;
import com.medbox.medboxbackend.model.MedBoxStack;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompartmentRepository extends CrudRepository<Compartment, Long> {
    @Query("SELECT c FROM Compartment c JOIN MedBox mb ON c MEMBER OF mb.compartments JOIN MedBoxStack mbs ON mb MEMBER OF mbs.boxes WHERE c.id = :id AND mbs.userId = :userId")
    Optional<Compartment> findByIdAndUserId(Long id, String userId);

    @Query("SELECT COUNT(c)>0 FROM Compartment c JOIN MedBox mb ON c MEMBER OF mb.compartments WHERE c.name = :name AND mb.id = (SELECT mb2.id FROM Compartment c2 JOIN MedBox mb2 ON c2 MEMBER OF mb2.compartments WHERE c2.id = :compartmentId)")
    boolean existsNameInSameMedBox(String name, Long compartmentId);

    @Query("SELECT mbs FROM MedBoxStack mbs JOIN MedBox mb ON mb MEMBER OF mbs.boxes JOIN Compartment c ON c MEMBER OF mb.compartments WHERE c.id = :compartmentId")
    Optional<MedBoxStack> findStackOfCompartment(Long compartmentId);
}

package com.medbox.medboxbackend.stacks;

import com.medbox.medboxbackend.model.MedBoxStack;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedBoxStackRepository extends CrudRepository<MedBoxStack, Long> {
    List<MedBoxStack> findAllByUserId(String userId);

    @Query("SELECT mbs FROM MedBoxStack mbs JOIN mbs.boxes mb WHERE mb.mac = :medBoxMACAddress")
    Optional<MedBoxStack> findMedBoxStackByMedBoxMACAddress(String medBoxMACAddress);

    Optional<MedBoxStack> findByNameAndUserId(String name, String userId);

    Optional<MedBoxStack> findByIdAndUserId(Long id, String userId);
}

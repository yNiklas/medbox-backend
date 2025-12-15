package com.medbox.medboxbackend.boxes;

import com.medbox.medboxbackend.model.MedBox;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedBoxRepository extends CrudRepository<MedBox, Long> {
    @Query("SELECT mb FROM MedBox mb JOIN MedBoxStack mbs ON mb MEMBER OF mbs.boxes WHERE mb.id = :id AND mbs.userId = :userId")
    Optional<MedBox> findByIdAndUserId(Long id, String userId);

    @Query("SELECT COUNT(mb)>0 FROM MedBox mb JOIN MedBoxStack mbs ON mb MEMBER OF mbs.boxes WHERE mb.name = :name AND mbs.id = (SELECT mbs2.id FROM MedBox mb2 JOIN MedBoxStack mbs2 ON mb2 MEMBER OF mbs2.boxes WHERE mb2.id = :boxId)")
    boolean existsNameInSameStack(String name, Long boxId);

    Optional<MedBox> findByMac(String mac);
}

package com.medbox.mexboxbackend.stacks;

import com.medbox.mexboxbackend.model.MedBoxStack;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedBoxStackRepository extends CrudRepository<MedBoxStack, Long> {
    List<MedBoxStack> findAllByUserId(String userId);
}

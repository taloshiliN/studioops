package org.fromdesertdev.studioops.validation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationDecisionRepository extends JpaRepository<ValidationDecision, Long> {
    List<ValidationDecision> findByGame_IdOrderByDecidedAtDesc(Long gameId);
}

package org.fromdesertdev.studioops.traction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TractionSnapshotRepository extends JpaRepository<TractionSnapshot, Long> {
    List<TractionSnapshot> findByGame_IdOrderByCapturedAtAsc(Long gameId);
    List<TractionSnapshot> findByGame_IdOrderByCapturedAtDesc(Long gameId);
    List<TractionSnapshot> findByPrototype_IdOrderByCapturedAtAsc(Long prototypeId);
}

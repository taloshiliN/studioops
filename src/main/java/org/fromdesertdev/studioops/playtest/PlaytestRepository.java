package org.fromdesertdev.studioops.playtest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaytestRepository extends JpaRepository<Playtest, Long> {
    List<Playtest> findByGame_IdOrderBySessionDateDesc(Long gameId);
}

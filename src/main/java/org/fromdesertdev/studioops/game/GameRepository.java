package org.fromdesertdev.studioops.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStudio_Id(Long studioId);
    List<Game> findByStudio_IdInOrderByCreatedAtDesc(List<Long> studioIds);
}

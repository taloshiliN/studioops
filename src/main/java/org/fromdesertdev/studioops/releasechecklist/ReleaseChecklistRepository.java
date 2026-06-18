package org.fromdesertdev.studioops.releasechecklist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReleaseChecklistRepository extends JpaRepository<ReleaseChecklistItem, Long> {
    List<ReleaseChecklistItem> findByGame_IdOrderByCreatedAtAsc(Long gameId);

    void deleteByGame_Id(Long gameId);
}

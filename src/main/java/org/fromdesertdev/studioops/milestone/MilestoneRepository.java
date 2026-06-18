package org.fromdesertdev.studioops.milestone;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByGame_IdOrderByDueDateAsc(Long gameId);

    void deleteByGame_Id(Long gameId);
}

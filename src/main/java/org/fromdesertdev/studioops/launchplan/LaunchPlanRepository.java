package org.fromdesertdev.studioops.launchplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaunchPlanRepository extends JpaRepository<LaunchPlan, Long> {
    Optional<LaunchPlan> findByGame_Id(Long gameId);
}

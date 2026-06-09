package org.fromdesertdev.studioops.marketing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketingActivityRepository extends JpaRepository<MarketingActivity, Long> {
    List<MarketingActivity> findByGame_IdOrderByScheduledForAscCreatedAtAsc(Long gameId);
}

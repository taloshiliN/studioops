package org.fromdesertdev.studioops.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudioMembershipRepository extends JpaRepository<StudioMembership, Long> {
    List<StudioMembership> findByStudio_IdOrderByCreatedAtAsc(Long studioId);
    List<StudioMembership> findByUser_Id(Long userId);
    Optional<StudioMembership> findByStudio_IdAndUser_Id(Long studioId, Long userId);
    boolean existsByStudio_IdAndUser_Id(Long studioId, Long userId);
}

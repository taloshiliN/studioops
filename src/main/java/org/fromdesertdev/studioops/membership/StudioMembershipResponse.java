package org.fromdesertdev.studioops.membership;

import java.time.LocalDateTime;

public record StudioMembershipResponse(
        Long id,
        Long studioId,
        Long userId,
        String userName,
        String userEmail,
        MembershipRole role,
        LocalDateTime createdAt
) {
    public static StudioMembershipResponse from(StudioMembership membership) {
        return new StudioMembershipResponse(
                membership.getId(),
                membership.getStudio().getId(),
                membership.getUser().getId(),
                membership.getUser().getName(),
                membership.getUser().getEmail(),
                membership.getRole(),
                membership.getCreatedAt()
        );
    }
}

package org.fromdesertdev.studioops.membership;

import jakarta.validation.constraints.NotNull;

public record AddStudioMemberRequest(
        @NotNull Long userId,
        @NotNull MembershipRole role
) {
}

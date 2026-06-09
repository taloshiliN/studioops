package org.fromdesertdev.studioops.membership;

import jakarta.validation.constraints.NotNull;

public record UpdateStudioMemberRoleRequest(
        @NotNull MembershipRole role
) {
}

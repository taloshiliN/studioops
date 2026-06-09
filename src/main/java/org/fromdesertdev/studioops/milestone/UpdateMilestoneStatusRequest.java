package org.fromdesertdev.studioops.milestone;

import jakarta.validation.constraints.NotNull;

public record UpdateMilestoneStatusRequest(
        @NotNull MilestoneStatus status
) {
}

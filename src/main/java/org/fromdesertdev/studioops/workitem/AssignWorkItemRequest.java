package org.fromdesertdev.studioops.workitem;

import jakarta.validation.constraints.NotNull;

public record AssignWorkItemRequest(
        @NotNull Long assigneeUserId
) {
}

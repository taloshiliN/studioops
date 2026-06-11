package org.fromdesertdev.studioops.workitem;

import jakarta.validation.constraints.NotNull;

public record UpdateWorkItemStatusRequest(
        @NotNull WorkItemStatus status
) {
}

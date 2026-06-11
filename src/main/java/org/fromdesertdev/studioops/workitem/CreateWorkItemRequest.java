package org.fromdesertdev.studioops.workitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateWorkItemRequest(
        Long milestoneId,
        Long assigneeUserId,
        @NotBlank @Size(max = 180) String title,
        @Size(max = 4000) String description,
        @NotNull WorkItemPriority priority,
        LocalDate dueDate
) {
}

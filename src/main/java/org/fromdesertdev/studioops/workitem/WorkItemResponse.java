package org.fromdesertdev.studioops.workitem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WorkItemResponse(
        Long id,
        Long gameId,
        Long milestoneId,
        Long assigneeUserId,
        String assigneeName,
        String title,
        String description,
        WorkItemStatus status,
        WorkItemPriority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WorkItemResponse from(WorkItem workItem){
        return new WorkItemResponse(
                workItem.getId(),
                workItem.getGame().getId(),
                workItem.getMilestone() == null ? null : workItem.getMilestone().getId(),
                workItem.getAssignee() == null ? null : workItem.getAssignee().getId(),
                workItem.getAssignee() == null ? null : workItem.getAssignee().getName(),
                workItem.getTitle(),
                workItem.getDescription(),
                workItem.getStatus(),
                workItem.getPriority(),
                workItem.getDueDate(),
                workItem.getCreatedAt(),
                workItem.getUpdatedAt()
        );
    }
}

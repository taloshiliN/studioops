package org.fromdesertdev.studioops.milestone;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MilestoneResponse(
        Long id,
        Long gameId,
        String name,
        LocalDate dueDate,
        MilestoneStatus status,
        LocalDateTime createdAt
) {
    public static MilestoneResponse from(Milestone milestone) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getGame().getId(),
                milestone.getName(),
                milestone.getDueDate(),
                milestone.getStatus(),
                milestone.getCreatedAt()
        );
    }
}

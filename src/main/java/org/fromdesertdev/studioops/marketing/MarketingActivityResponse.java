package org.fromdesertdev.studioops.marketing;

import java.time.LocalDateTime;

public record MarketingActivityResponse(
        Long id,
        Long gameId,
        MarketingActivityType activityType,
        String channel,
        String title,
        LocalDateTime scheduledFor,
        LocalDateTime completedAt,
        String resultNotes,
        LocalDateTime createdAt
) {
    public static MarketingActivityResponse from(MarketingActivity activity) {
        return new MarketingActivityResponse(
                activity.getId(),
                activity.getGame().getId(),
                activity.getActivityType(),
                activity.getChannel(),
                activity.getTitle(),
                activity.getScheduledFor(),
                activity.getCompletedAt(),
                activity.getResultNotes(),
                activity.getCreatedAt()
        );
    }
}

package org.fromdesertdev.studioops.releasechecklist;

import java.util.List;

public record ReleaseReadinessResponse(
        Long gameId,
        int totalItems,
        int completedItems,
        int readinessPercentage,
        boolean blocked,
        List<String> blockingItems
) {
}

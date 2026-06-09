package org.fromdesertdev.studioops.releasechecklist;

import java.time.LocalDateTime;

public record ReleaseChecklistItemResponse(
        Long id,
        Long gameId,
        String title,
        String description,
        boolean completed,
        boolean blocksRelease,
        LocalDateTime createdAt
) {
    public static ReleaseChecklistItemResponse from(ReleaseChecklistItem item) {
        return new ReleaseChecklistItemResponse(
                item.getId(),
                item.getGame().getId(),
                item.getTitle(),
                item.getDescription(),
                item.isCompleted(),
                item.isBlocksRelease(),
                item.getCreatedAt()
        );
    }
}

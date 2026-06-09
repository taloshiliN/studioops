package org.fromdesertdev.studioops.studio;

import java.time.LocalDateTime;

public record StudioResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {
    public static StudioResponse from (Studio studio){
        return new StudioResponse(
                studio.getId(),
                studio.GetName(),
                studio.getCreatedAt()
        );
    }
}

package org.fromdesertdev.studioops.gamejam;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GameJamResponse(
        Long id,
        String name,
        String host,
        String theme,
        LocalDate startDate,
        LocalDate endDate,
        String url,
        LocalDateTime createdAt
) {
    public static GameJamResponse from(GameJam gameJam){
        return new GameJamResponse(
                gameJam.getId(),
                gameJam.getName(),
                gameJam.getHost(),
                gameJam.getTheme(),
                gameJam.getStartDate(),
                gameJam.getEndDate(),
                gameJam.getUrl(),
                gameJam.getCreatedAt()
        );
    }
}

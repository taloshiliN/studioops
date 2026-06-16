package org.fromdesertdev.studioops.game;

import java.time.LocalDateTime;

public record GameResponse(
        Long id,
        Long studioId,
        String title,
        String shortPitch,
        String genre,
        GameStage currentStage,
        ValidationStatus validationStatus,
        String targetPlatforms,
        String fontFamily,
        LocalDateTime createdAt
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getStudio().getId(),
                game.getTitle(),
                game.getShortPitch(),
                game.getGenre(),
                game.getCurrentStage(),
                game.getValidationStatus(),
                game.getTargetPlatforms(),
                game.getFontFamily(),
                game.getCreatedAt()
        );
    }
}

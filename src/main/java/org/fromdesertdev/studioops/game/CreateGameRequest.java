package org.fromdesertdev.studioops.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGameRequest(
        @NotNull Long studioId,
        @NotBlank @Size(max = 160) String title,
        String shortPitch,
        @Size(max = 80) String genre,
        @Size(max = 240) String targetPlatforms
) {
}

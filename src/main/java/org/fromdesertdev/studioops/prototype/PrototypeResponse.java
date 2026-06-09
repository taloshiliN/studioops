package org.fromdesertdev.studioops.prototype;

import java.time.LocalDateTime;

public record PrototypeResponse(
        Long id,
        Long gameId,
        Long gameJamId,
        String name,
        String buildVersion,
        String itchUrl,
        String repositoryUrl,
        String playableUrl,
        LocalDateTime createdAt
) {
    public static PrototypeResponse from(Prototype prototype) {
        Long gameJamId = prototype.getGameJam() == null
                ? null
                : prototype.getGameJam().getId();

        return new PrototypeResponse(
                prototype.getId(),
                prototype.getGame().getId(),
                gameJamId,
                prototype.getName(),
                prototype.getBuildVersion(),
                prototype.getItchUrl(),
                prototype.getRepositoryUrl(),
                prototype.getPlayableUrl(),
                prototype.getCreatedAt()
        );
    }
}

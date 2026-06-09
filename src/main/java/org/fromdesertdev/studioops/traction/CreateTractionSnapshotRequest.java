package org.fromdesertdev.studioops.traction;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTractionSnapshotRequest(
        @NotNull Long gameId,
        Long prototypeId,
        @NotNull TractionSource source,
        @PositiveOrZero int views,
        @PositiveOrZero int downloads,
        @PositiveOrZero int plays,
        @PositiveOrZero int ratingsCount,
        @DecimalMin("0.00") @DecimalMax("5.00") BigDecimal averageRating,
        @PositiveOrZero int commentsCount,
        @PositiveOrZero int followersGained,
        @PositiveOrZero int wishlists,
        @PositiveOrZero int revenueCents,
        LocalDateTime capturedAt
) {
}

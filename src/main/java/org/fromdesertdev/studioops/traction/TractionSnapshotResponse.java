package org.fromdesertdev.studioops.traction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TractionSnapshotResponse(
        Long id,
        Long gameId,
        Long prototypeId,
        TractionSource source,
        int views,
        int downloads,
        int plays,
        int ratingsCount,
        BigDecimal averageRating,
        int commentsCount,
        int followersGained,
        int wishlists,
        int revenueCents,
        LocalDateTime capturedAt
) {
    public static TractionSnapshotResponse from(TractionSnapshot snapshot) {
        Long prototypeId = snapshot.getPrototype() == null
                ? null
                : snapshot.getPrototype().getId();

        return new TractionSnapshotResponse(
                snapshot.getId(),
                snapshot.getGame().getId(),
                prototypeId,
                snapshot.getSource(),
                snapshot.getViews(),
                snapshot.getDownloads(),
                snapshot.getPlays(),
                snapshot.getRatingsCount(),
                snapshot.getAverageRating(),
                snapshot.getCommentsCount(),
                snapshot.getFollowersGained(),
                snapshot.getWishlists(),
                snapshot.getRevenueCents(),
                snapshot.getCapturedAt()
        );
    }
}

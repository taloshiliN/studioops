package org.fromdesertdev.studioops.traction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.prototype.Prototype;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "traction_snapshots")
public class TractionSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prototype_id")
    private Prototype prototype;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TractionSource source;

    @Column(nullable = false)
    private int views;

    @Column(nullable = false)
    private int downloads;

    @Column(nullable = false)
    private int plays;

    @Column(name = "ratings_count", nullable = false)
    private int ratingsCount;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount;

    @Column(name = "followers_gained", nullable = false)
    private int followersGained;

    @Column(nullable = false)
    private int wishlists;

    @Column(name = "revenue_cents", nullable = false)
    private int revenueCents;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    protected TractionSnapshot() {
    }

    public TractionSnapshot(
            Game game,
            Prototype prototype,
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
        this.game = game;
        this.prototype = prototype;
        this.source = source;
        this.views = views;
        this.downloads = downloads;
        this.plays = plays;
        this.ratingsCount = ratingsCount;
        this.averageRating = averageRating;
        this.commentsCount = commentsCount;
        this.followersGained = followersGained;
        this.wishlists = wishlists;
        this.revenueCents = revenueCents;
        this.capturedAt = capturedAt == null ? LocalDateTime.now() : capturedAt;
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public Prototype getPrototype() { return prototype; }
    public TractionSource getSource() { return source; }
    public int getViews() { return views; }
    public int getDownloads() { return downloads; }
    public int getPlays() { return plays; }
    public int getRatingsCount() { return ratingsCount; }
    public BigDecimal getAverageRating() { return averageRating; }
    public int getCommentsCount() { return commentsCount; }
    public int getFollowersGained() { return followersGained; }
    public int getWishlists() { return wishlists; }
    public int getRevenueCents() { return revenueCents; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
}

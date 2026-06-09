package org.fromdesertdev.studioops.marketing;

import jakarta.persistence.*;
import org.fromdesertdev.studioops.game.Game;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_activities")
public class MarketingActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private MarketingActivityType activityType;

    @Column(nullable = false, length = 80)
    private String channel;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "result_notes")
    private String resultNotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected MarketingActivity() {
    }

    public MarketingActivity(
            Game game,
            MarketingActivityType activityType,
            String channel,
            String title,
            LocalDateTime scheduledFor
    ) {
        this.game = game;
        this.activityType = activityType;
        this.channel = channel;
        this.title = title;
        this.scheduledFor = scheduledFor;
        this.createdAt = LocalDateTime.now();
    }

    public void complete(String resultNotes) {
        this.completedAt = LocalDateTime.now();
        this.resultNotes = resultNotes;
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public MarketingActivityType getActivityType() { return activityType; }
    public String getChannel() { return channel; }
    public String getTitle() { return title; }
    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getResultNotes() { return resultNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

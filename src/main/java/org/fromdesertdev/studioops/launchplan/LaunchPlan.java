package org.fromdesertdev.studioops.launchplan;

import jakarta.persistence.*;
import org.fromdesertdev.studioops.game.Game;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "launch_plans")
public class LaunchPlan {
    static final int DEFAULT_CONTENT_CREATOR_TARGET = 300;
    static final int DEFAULT_FESTIVAL_TARGET = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false, unique = true)
    private Game game;

    @Column(name = "itch_page_url", length = 500)
    private String itchPageUrl;

    @Column(name = "steam_page_url", length = 500)
    private String steamPageUrl;

    @Column(name = "demo_url", length = 500)
    private String demoUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "target_demo_date")
    private LocalDate targetDemoDate;

    @Column(name = "target_next_fest_date")
    private LocalDate targetNextFestDate;

    @Column(name = "target_launch_date")
    private LocalDate targetLaunchDate;

    @Column(name = "content_creator_outreach_target", nullable = false)
    private int contentCreatorOutreachTarget = DEFAULT_CONTENT_CREATOR_TARGET;

    @Column(name = "festival_submission_target", nullable = false)
    private int festivalSubmissionTarget = DEFAULT_FESTIVAL_TARGET;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected LaunchPlan() {
    }

    public LaunchPlan(Game game) {
        this.game = game;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(
            String itchPageUrl,
            String steamPageUrl,
            String demoUrl,
            String trailerUrl,
            LocalDate targetDemoDate,
            LocalDate targetNextFestDate,
            LocalDate targetLaunchDate,
            Integer contentCreatorOutreachTarget,
            Integer festivalSubmissionTarget,
            String notes
    ) {
        this.itchPageUrl = normalizeText(itchPageUrl);
        this.steamPageUrl = normalizeText(steamPageUrl);
        this.demoUrl = normalizeText(demoUrl);
        this.trailerUrl = normalizeText(trailerUrl);
        this.targetDemoDate = targetDemoDate;
        this.targetNextFestDate = targetNextFestDate;
        this.targetLaunchDate = targetLaunchDate;
        this.contentCreatorOutreachTarget = normalizeTarget(
                contentCreatorOutreachTarget,
                DEFAULT_CONTENT_CREATOR_TARGET
        );
        this.festivalSubmissionTarget = normalizeTarget(
                festivalSubmissionTarget,
                DEFAULT_FESTIVAL_TARGET
        );
        this.notes = normalizeText(notes);
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public String getItchPageUrl() {
        return itchPageUrl;
    }

    public String getSteamPageUrl() {
        return steamPageUrl;
    }

    public String getDemoUrl() {
        return demoUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public LocalDate getTargetDemoDate() {
        return targetDemoDate;
    }

    public LocalDate getTargetNextFestDate() {
        return targetNextFestDate;
    }

    public LocalDate getTargetLaunchDate() {
        return targetLaunchDate;
    }

    public int getContentCreatorOutreachTarget() {
        return contentCreatorOutreachTarget;
    }

    public int getFestivalSubmissionTarget() {
        return festivalSubmissionTarget;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private int normalizeTarget(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}

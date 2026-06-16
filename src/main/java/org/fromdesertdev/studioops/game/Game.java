package org.fromdesertdev.studioops.game;

import jakarta.persistence.*;
import org.fromdesertdev.studioops.studio.Studio;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {
    private static final String DEFAULT_FONT_FAMILY = "Inter";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "short_pitch")
    private String shortPitch;

    @Column(length = 80)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 40)
    private GameStage currentStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 40)
    private ValidationStatus validationStatus;

    @Column(name = "target_platforms", length = 240)
    private String targetPlatforms;

    @Column(name = "font_family", nullable = false, length = 80)
    private String fontFamily = DEFAULT_FONT_FAMILY;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Game() {

    }

    public Game(
            Studio studio,
            String title,
            String shortPitch,
            String genre,
            String targetPlatforms,
            String fontFamily
    ) {
        this.studio = studio;
        this.title = title;
        this.shortPitch = shortPitch;
        this.genre = genre;
        this.targetPlatforms = targetPlatforms;
        this.fontFamily = normalizeFontFamily(fontFamily);
        this.currentStage = GameStage.IDEA;
        this.validationStatus = ValidationStatus.UNVALIDATED;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Studio getStudio() {
        return studio;
    }

    public String getTitle() {
        return title;
    }

    public String getShortPitch() {
        return shortPitch;
    }

    public String getGenre() {
        return genre;
    }

    public GameStage getCurrentStage() {
        return currentStage;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public String getTargetPlatforms() {
        return targetPlatforms;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateValidationState(GameStage stage, ValidationStatus status) {
        this.currentStage = stage;
        this.validationStatus = status;
    }

    public void moveToStage(GameStage stage) {
        this.currentStage = stage;
    }

    private String normalizeFontFamily(String fontFamily) {
        if (fontFamily == null || fontFamily.isBlank()) {
            return DEFAULT_FONT_FAMILY;
        }

        return fontFamily.trim();
    }
}

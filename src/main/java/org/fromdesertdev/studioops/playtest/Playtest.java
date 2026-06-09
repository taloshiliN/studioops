package org.fromdesertdev.studioops.playtest;

import jakarta.persistence.*;
import org.fromdesertdev.studioops.game.Game;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "playtests")
public class Playtest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "tester_group", length = 160)
    private String testerGroup;

    @Column(name = "build_version", length = 80)
    private String buildVersion;

    private String notes;

    @Column(name = "main_findings")
    private String mainFindings;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Playtest() {
    }

    public Playtest(Game game, LocalDate sessionDate, String testerGroup,
                    String buildVersion, String notes, String mainFindings) {
        this.game = game;
        this.sessionDate = sessionDate;
        this.testerGroup = testerGroup;
        this.buildVersion = buildVersion;
        this.notes = notes;
        this.mainFindings = mainFindings;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public LocalDate getSessionDate() { return sessionDate; }
    public String getTesterGroup() { return testerGroup; }
    public String getBuildVersion() { return buildVersion; }
    public String getNotes() { return notes; }
    public String getMainFindings() { return mainFindings; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

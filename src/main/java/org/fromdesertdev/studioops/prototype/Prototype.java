package org.fromdesertdev.studioops.prototype;

import jakarta.persistence.*;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.gamejam.GameJam;

import java.time.LocalDateTime;

@Entity
@Table(name = "prototypes")
public class Prototype {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_jam_id")
    private GameJam gameJam;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "build_version", length = 80)
    private String buildVersion;

    @Column(name = "itch_url", length = 500)
    private String itchUrl;

    @Column(name = "repository_url", length = 500)
    private String repositoryUrl;

    @Column(name = "playable_url", length = 500)
    private String playableUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Prototype() {
    }

    public Prototype(
            Game game,
            GameJam gameJam,
            String name,
            String buildVersion,
            String itchUrl,
            String repositoryUrl,
            String playableUrl
    ) {
        this.game = game;
        this.gameJam = gameJam;
        this.name = name;
        this.buildVersion = buildVersion;
        this.itchUrl = itchUrl;
        this.repositoryUrl = repositoryUrl;
        this.playableUrl = playableUrl;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public GameJam getGameJam() { return gameJam; }
    public String getName() { return name; }
    public String getBuildVersion() { return buildVersion; }
    public String getItchUrl() { return itchUrl; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public String getPlayableUrl() { return playableUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

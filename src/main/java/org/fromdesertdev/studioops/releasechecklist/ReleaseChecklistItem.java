package org.fromdesertdev.studioops.releasechecklist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.fromdesertdev.studioops.game.Game;

import java.time.LocalDateTime;

@Entity
@Table(name = "release_checklist_items")
public class ReleaseChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false, length = 160)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "blocks_release", nullable = false)
    private boolean blocksRelease;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ReleaseChecklistItem() {
    }

    public ReleaseChecklistItem(Game game, String title, String description, boolean blocksRelease) {
        this.game = game;
        this.title = title;
        this.description = description;
        this.completed = false;
        this.blocksRelease = blocksRelease;
        this.createdAt = LocalDateTime.now();
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public boolean isBlocksRelease() { return blocksRelease; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

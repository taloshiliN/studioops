package org.fromdesertdev.studioops.milestone;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "milestones")
public class Milestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MilestoneStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Milestone() {
    }

    public Milestone(Game game, String name, LocalDate dueDate) {
        this.game = game;
        this.name = name;
        this.dueDate = dueDate;
        this.status = MilestoneStatus.PLANNED;
        this.createdAt = LocalDateTime.now();
    }

    public void changeStatus(MilestoneStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public String getName() { return name; }
    public LocalDate getDueDate() { return dueDate; }
    public MilestoneStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

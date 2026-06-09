package org.fromdesertdev.studioops.validation;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "validation_decisions")
public class ValidationDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ValidationDecisionType decision;

    @Column
    private String reason;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    protected ValidationDecision() {
    }

    public ValidationDecision(Game game, ValidationDecisionType decision, String reason) {
        this.game = game;
        this.decision = decision;
        this.reason = reason;
        this.decidedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public ValidationDecisionType getDecision() { return decision; }
    public String getReason() { return reason; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
}

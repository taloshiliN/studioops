package org.fromdesertdev.studioops.validation;

import org.fromdesertdev.studioops.game.GameStage;
import org.fromdesertdev.studioops.game.ValidationStatus;

import java.time.LocalDateTime;

public record ValidationDecisionResponse(
        Long id,
        Long gameId,
        ValidationDecisionType decision,
        String reason,
        GameStage currentStage,
        ValidationStatus validationStatus,
        LocalDateTime decidedAt
) {
    public static ValidationDecisionResponse from(ValidationDecision validationDecision) {
        return new ValidationDecisionResponse(
                validationDecision.getId(),
                validationDecision.getGame().getId(),
                validationDecision.getDecision(),
                validationDecision.getReason(),
                validationDecision.getGame().getCurrentStage(),
                validationDecision.getGame().getValidationStatus(),
                validationDecision.getDecidedAt()
        );
    }
}

package org.fromdesertdev.studioops.validation;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.game.GameStage;
import org.fromdesertdev.studioops.game.ValidationStatus;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ValidationDecisionService {
    private final ValidationDecisionRepository validationDecisionRepository;
    private final GameRepository gameRepository;
    private final PermissionService permissionService;

    public ValidationDecisionService(
            ValidationDecisionRepository validationDecisionRepository,
            GameRepository gameRepository,
            PermissionService permissionService
    ) {
        this.validationDecisionRepository = validationDecisionRepository;
        this.gameRepository = gameRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public ValidationDecisionResponse create(Long gameId, CreateValidationDecisionRequest request) {
        permissionService.requireGameRole(
                gameId,
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        applyDecision(game, request.decision());

        ValidationDecision validationDecision = new ValidationDecision(
                game,
                request.decision(),
                request.reason()
        );

        return ValidationDecisionResponse.from(validationDecisionRepository.save(validationDecision));
    }

    @Transactional(readOnly = true)
    public List<ValidationDecisionResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);

        if (!gameRepository.existsById(gameId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        return validationDecisionRepository.findByGame_IdOrderByDecidedAtDesc(gameId)
                .stream()
                .map(ValidationDecisionResponse::from)
                .toList();
    }

    private void applyDecision(Game game, ValidationDecisionType decision) {
        switch (decision) {
            case GREENLIGHT -> game.updateValidationState(GameStage.PLANNING, ValidationStatus.VALIDATED);
            case PIVOT -> game.updateValidationState(GameStage.PROTOTYPE, ValidationStatus.NEEDS_MORE_TESTING);
            case NEEDS_MORE_TESTING -> game.updateValidationState(
                    GameStage.VALIDATION,
                    ValidationStatus.NEEDS_MORE_TESTING
            );
            case SHELVE -> game.updateValidationState(GameStage.SHELVED, ValidationStatus.SHELVED);
        }
    }
}

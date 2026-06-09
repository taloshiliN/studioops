package org.fromdesertdev.studioops.milestone;

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
import java.util.Set;

@Service
public class MilestoneService {
    private final MilestoneRepository milestoneRepository;
    private final GameRepository gameRepository;
    private final PermissionService permissionService;

    public MilestoneService(
            MilestoneRepository milestoneRepository,
            GameRepository gameRepository,
            PermissionService permissionService
    ) {
        this.milestoneRepository = milestoneRepository;
        this.gameRepository = gameRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public MilestoneResponse create(Long gameId, CreateMilestoneRequest request) {
        permissionService.requireGameRole(
                gameId,
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );

        Game game = findGame(gameId);

        if (game.getValidationStatus() != ValidationStatus.VALIDATED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Milestones can only be created for a validated game"
            );
        }

        Milestone milestone = new Milestone(game, request.name(), request.dueDate());
        return MilestoneResponse.from(milestoneRepository.save(milestone));
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);
        findGame(gameId);

        return milestoneRepository.findByGame_IdOrderByDueDateAsc(gameId)
                .stream()
                .map(MilestoneResponse::from)
                .toList();
    }

    @Transactional
    public MilestoneResponse updateStatus(Long milestoneId, UpdateMilestoneStatusRequest request) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Milestone not found"));

        permissionService.requireGameRole(
                milestone.getGame().getId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );

        validateTransition(milestone.getStatus(), request.status());
        milestone.changeStatus(request.status());

        if (request.status() == MilestoneStatus.IN_PROGRESS) {
            milestone.getGame().moveToStage(GameStage.PRODUCTION);
        }

        return MilestoneResponse.from(milestone);
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private void validateTransition(MilestoneStatus current, MilestoneStatus requested) {
        if (current == requested) {
            return;
        }

        Set<MilestoneStatus> allowed = switch (current) {
            case PLANNED -> Set.of(
                    MilestoneStatus.IN_PROGRESS,
                    MilestoneStatus.BLOCKED,
                    MilestoneStatus.CANCELLED
            );
            case IN_PROGRESS -> Set.of(
                    MilestoneStatus.COMPLETED,
                    MilestoneStatus.BLOCKED,
                    MilestoneStatus.CANCELLED
            );
            case BLOCKED -> Set.of(MilestoneStatus.IN_PROGRESS, MilestoneStatus.CANCELLED);
            case COMPLETED, CANCELLED -> Set.of();
        };

        if (!allowed.contains(requested)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot move milestone from " + current + " to " + requested
            );
        }
    }
}

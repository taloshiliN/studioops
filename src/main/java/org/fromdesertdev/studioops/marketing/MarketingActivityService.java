package org.fromdesertdev.studioops.marketing;

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
public class MarketingActivityService {
    private final MarketingActivityRepository marketingActivityRepository;
    private final GameRepository gameRepository;
    private final PermissionService permissionService;

    public MarketingActivityService(
            MarketingActivityRepository marketingActivityRepository,
            GameRepository gameRepository,
            PermissionService permissionService
    ) {
        this.marketingActivityRepository = marketingActivityRepository;
        this.gameRepository = gameRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public MarketingActivityResponse create(Long gameId, CreateMarketingActivityRequest request) {
        permissionService.requireGameRole(
                gameId,
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        Game game = findGame(gameId);

        if (game.getValidationStatus() != ValidationStatus.VALIDATED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Marketing activities can only be created for a validated game"
            );
        }

        MarketingActivity activity = new MarketingActivity(
                game,
                request.activityType(),
                request.channel(),
                request.title(),
                request.scheduledFor()
        );

        game.moveToStage(GameStage.MARKETING);
        return MarketingActivityResponse.from(marketingActivityRepository.save(activity));
    }

    @Transactional(readOnly = true)
    public List<MarketingActivityResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);
        findGame(gameId);

        return marketingActivityRepository.findByGame_IdOrderByScheduledForAscCreatedAtAsc(gameId)
                .stream()
                .map(MarketingActivityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MarketingActivityResponse findById(Long id) {
        MarketingActivity activity = findActivity(id);
        permissionService.requireGameMember(activity.getGame().getId());

        return MarketingActivityResponse.from(activity);
    }

    @Transactional
    public MarketingActivityResponse complete(Long id, CompleteMarketingActivityRequest request) {
        MarketingActivity activity = findActivity(id);

        permissionService.requireGameRole(
                activity.getGame().getId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        activity.complete(request.resultNotes());
        return MarketingActivityResponse.from(activity);
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private MarketingActivity findActivity(Long id) {
        return marketingActivityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Marketing activity not found"
                ));
    }
}

package org.fromdesertdev.studioops.launchplan;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LaunchPlanService {
    private final LaunchPlanRepository launchPlanRepository;
    private final GameRepository gameRepository;
    private final PermissionService permissionService;

    public LaunchPlanService(
            LaunchPlanRepository launchPlanRepository,
            GameRepository gameRepository,
            PermissionService permissionService
    ) {
        this.launchPlanRepository = launchPlanRepository;
        this.gameRepository = gameRepository;
        this.permissionService = permissionService;
    }

    @Transactional(readOnly = true)
    public LaunchPlanResponse findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);

        return launchPlanRepository.findByGame_Id(gameId)
                .map(LaunchPlanResponse::from)
                .orElseGet(() -> LaunchPlanResponse.empty(gameId));
    }

    @Transactional
    public LaunchPlanResponse upsert(Long gameId, UpsertLaunchPlanRequest request) {
        permissionService.requireGameRole(
                gameId,
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        LaunchPlan plan = launchPlanRepository.findByGame_Id(gameId)
                .orElseGet(() -> new LaunchPlan(game));

        plan.update(
                request.itchPageUrl(),
                request.steamPageUrl(),
                request.demoUrl(),
                request.trailerUrl(),
                request.targetDemoDate(),
                request.targetNextFestDate(),
                request.targetLaunchDate(),
                request.contentCreatorOutreachTarget(),
                request.festivalSubmissionTarget(),
                request.notes()
        );

        return LaunchPlanResponse.from(launchPlanRepository.save(plan));
    }
}

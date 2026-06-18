package org.fromdesertdev.studioops.game;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.launchplan.LaunchPlanRepository;
import org.fromdesertdev.studioops.marketing.MarketingActivityRepository;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.fromdesertdev.studioops.membership.StudioMembershipRepository;
import org.fromdesertdev.studioops.milestone.MilestoneRepository;
import org.fromdesertdev.studioops.playtest.PlaytestRepository;
import org.fromdesertdev.studioops.prototype.PrototypeRepository;
import org.fromdesertdev.studioops.releasechecklist.ReleaseChecklistRepository;
import org.fromdesertdev.studioops.studio.Studio;
import org.fromdesertdev.studioops.studio.StudioRepository;
import org.fromdesertdev.studioops.traction.TractionSnapshotRepository;
import org.fromdesertdev.studioops.user.AppUser;
import org.fromdesertdev.studioops.validation.ValidationDecisionRepository;
import org.fromdesertdev.studioops.workitem.WorkItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final StudioRepository studioRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final PermissionService permissionService;
    private final LaunchPlanRepository launchPlanRepository;
    private final MarketingActivityRepository marketingActivityRepository;
    private final MilestoneRepository milestoneRepository;
    private final PlaytestRepository playtestRepository;
    private final PrototypeRepository prototypeRepository;
    private final ReleaseChecklistRepository releaseChecklistRepository;
    private final TractionSnapshotRepository tractionSnapshotRepository;
    private final ValidationDecisionRepository validationDecisionRepository;
    private final WorkItemRepository workItemRepository;

    public GameService(
            GameRepository gameRepository,
            StudioRepository studioRepository,
            StudioMembershipRepository studioMembershipRepository,
            PermissionService permissionService,
            LaunchPlanRepository launchPlanRepository,
            MarketingActivityRepository marketingActivityRepository,
            MilestoneRepository milestoneRepository,
            PlaytestRepository playtestRepository,
            PrototypeRepository prototypeRepository,
            ReleaseChecklistRepository releaseChecklistRepository,
            TractionSnapshotRepository tractionSnapshotRepository,
            ValidationDecisionRepository validationDecisionRepository,
            WorkItemRepository workItemRepository
    ) {
        this.gameRepository = gameRepository;
        this.studioRepository = studioRepository;
        this.studioMembershipRepository = studioMembershipRepository;
        this.permissionService = permissionService;
        this.launchPlanRepository = launchPlanRepository;
        this.marketingActivityRepository = marketingActivityRepository;
        this.milestoneRepository = milestoneRepository;
        this.playtestRepository = playtestRepository;
        this.prototypeRepository = prototypeRepository;
        this.releaseChecklistRepository = releaseChecklistRepository;
        this.tractionSnapshotRepository = tractionSnapshotRepository;
        this.validationDecisionRepository = validationDecisionRepository;
        this.workItemRepository = workItemRepository;
    }

    @Transactional
    public GameResponse create(CreateGameRequest request) {
        Studio studio = studioRepository.findById(request.studioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studio not found"));

        permissionService.requireStudioRole(
                request.studioId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        Game game = new Game(
                studio,
                request.title(),
                request.shortPitch(),
                request.genre(),
                request.targetPlatforms(),
                request.fontFamily()
        );

        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findAll() {
        AppUser user = permissionService.requireCurrentUser();

        List<Long> studioIds = studioMembershipRepository.findByUser_Id(user.getId())
                .stream()
                .map(membership -> membership.getStudio().getId())
                .toList();

        return gameRepository.findByStudio_IdInOrderByCreatedAtDesc(studioIds)
                .stream()
                .map(GameResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameResponse findById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        permissionService.requireGameMember(id);

        return GameResponse.from(game);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findByStudio(Long studioId) {
        permissionService.requireStudioMember(studioId);

        return gameRepository.findByStudio_Id(studioId)
                .stream()
                .map(GameResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        permissionService.requireStudioRole(
                game.getStudio().getId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        Long gameId = game.getId();
        workItemRepository.deleteByGame_Id(gameId);
        launchPlanRepository.deleteByGame_Id(gameId);
        marketingActivityRepository.deleteByGame_Id(gameId);
        playtestRepository.deleteByGame_Id(gameId);
        releaseChecklistRepository.deleteByGame_Id(gameId);
        tractionSnapshotRepository.deleteByGame_Id(gameId);
        validationDecisionRepository.deleteByGame_Id(gameId);
        prototypeRepository.deleteByGame_Id(gameId);
        milestoneRepository.deleteByGame_Id(gameId);
        gameRepository.delete(game);
    }
}

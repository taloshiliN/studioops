package org.fromdesertdev.studioops.dashboard;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.marketing.MarketingActivity;
import org.fromdesertdev.studioops.marketing.MarketingActivityRepository;
import org.fromdesertdev.studioops.milestone.Milestone;
import org.fromdesertdev.studioops.milestone.MilestoneRepository;
import org.fromdesertdev.studioops.milestone.MilestoneStatus;
import org.fromdesertdev.studioops.playtest.Playtest;
import org.fromdesertdev.studioops.playtest.PlaytestRepository;
import org.fromdesertdev.studioops.releasechecklist.ReleaseChecklistService;
import org.fromdesertdev.studioops.releasechecklist.ReleaseReadinessResponse;
import org.fromdesertdev.studioops.traction.TractionSnapshot;
import org.fromdesertdev.studioops.traction.TractionSnapshotRepository;
import org.fromdesertdev.studioops.validation.ValidationDecision;
import org.fromdesertdev.studioops.validation.ValidationDecisionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class GameDashboardService {
    private final GameRepository gameRepository;
    private final ValidationDecisionRepository validationDecisionRepository;
    private final TractionSnapshotRepository tractionSnapshotRepository;
    private final MilestoneRepository milestoneRepository;
    private final PlaytestRepository playtestRepository;
    private final MarketingActivityRepository marketingActivityRepository;
    private final ReleaseChecklistService releaseChecklistService;
    private final PermissionService permissionService;

    public GameDashboardService(
            GameRepository gameRepository,
            ValidationDecisionRepository validationDecisionRepository,
            TractionSnapshotRepository tractionSnapshotRepository,
            MilestoneRepository milestoneRepository,
            PlaytestRepository playtestRepository,
            MarketingActivityRepository marketingActivityRepository,
            ReleaseChecklistService releaseChecklistService,
            PermissionService permissionService
    ) {
        this.gameRepository = gameRepository;
        this.validationDecisionRepository = validationDecisionRepository;
        this.tractionSnapshotRepository = tractionSnapshotRepository;
        this.milestoneRepository = milestoneRepository;
        this.playtestRepository = playtestRepository;
        this.marketingActivityRepository = marketingActivityRepository;
        this.releaseChecklistService = releaseChecklistService;
        this.permissionService = permissionService;
    }

    @Transactional(readOnly = true)
    public GameDashboardResponse getDashboard(Long gameId) {
        permissionService.requireGameMember(gameId);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        return new GameDashboardResponse(
                gameSummary(game),
                validationSummary(gameId),
                tractionSummary(gameId),
                milestoneSummary(gameId),
                playtestSummary(gameId),
                marketingSummary(gameId),
                releaseReadinessSummary(gameId)
        );
    }

    private GameDashboardResponse.GameSummary gameSummary(Game game) {
        return new GameDashboardResponse.GameSummary(
                game.getId(),
                game.getTitle(),
                game.getCurrentStage(),
                game.getValidationStatus()
        );
    }

    private GameDashboardResponse.ValidationSummary validationSummary(Long gameId) {
        return validationDecisionRepository.findByGame_IdOrderByDecidedAtDesc(gameId)
                .stream()
                .findFirst()
                .map(decision -> new GameDashboardResponse.ValidationSummary(
                        decision.getDecision(),
                        decision.getReason(),
                        decision.getDecidedAt()
                ))
                .orElse(null);
    }

    private GameDashboardResponse.TractionSummary tractionSummary(Long gameId) {
        return tractionSnapshotRepository.findByGame_IdOrderByCapturedAtDesc(gameId)
                .stream()
                .findFirst()
                .map(snapshot -> new GameDashboardResponse.TractionSummary(
                        snapshot.getSource(),
                        snapshot.getViews(),
                        snapshot.getDownloads(),
                        snapshot.getPlays(),
                        snapshot.getRatingsCount(),
                        snapshot.getAverageRating(),
                        snapshot.getCommentsCount(),
                        snapshot.getFollowersGained(),
                        snapshot.getWishlists(),
                        snapshot.getRevenueCents(),
                        snapshot.getCapturedAt()
                ))
                .orElse(null);
    }

    private GameDashboardResponse.MilestoneSummary milestoneSummary(Long gameId) {
        List<Milestone> milestones = milestoneRepository.findByGame_IdOrderByDueDateAsc(gameId);

        return new GameDashboardResponse.MilestoneSummary(
                milestones.size(),
                countMilestones(milestones, MilestoneStatus.COMPLETED),
                countMilestones(milestones, MilestoneStatus.IN_PROGRESS),
                countMilestones(milestones, MilestoneStatus.BLOCKED)
        );
    }

    private int countMilestones(List<Milestone> milestones, MilestoneStatus status) {
        return (int) milestones.stream()
                .filter(milestone -> milestone.getStatus() == status)
                .count();
    }

    private GameDashboardResponse.PlaytestSummary playtestSummary(Long gameId) {
        List<Playtest> playtests = playtestRepository.findByGame_IdOrderBySessionDateDesc(gameId);
        Playtest latest = playtests.stream().findFirst().orElse(null);

        return new GameDashboardResponse.PlaytestSummary(
                playtests.size(),
                latest == null ? null : latest.getSessionDate(),
                latest == null ? null : latest.getBuildVersion(),
                latest == null ? null : latest.getMainFindings()
        );
    }

    private GameDashboardResponse.MarketingSummary marketingSummary(Long gameId) {
        List<MarketingActivity> activities =
                marketingActivityRepository.findByGame_IdOrderByScheduledForAscCreatedAtAsc(gameId);

        int completed = (int) activities.stream()
                .filter(activity -> activity.getCompletedAt() != null)
                .count();

        MarketingActivity next = activities.stream()
                .filter(activity -> activity.getCompletedAt() == null)
                .min(Comparator.comparing(
                        MarketingActivity::getScheduledFor,
                        Comparator.nullsLast(LocalDateTime::compareTo)
                ))
                .orElse(null);

        return new GameDashboardResponse.MarketingSummary(
                activities.size(),
                completed,
                activities.size() - completed,
                next == null ? null : next.getActivityType(),
                next == null ? null : next.getChannel(),
                next == null ? null : next.getTitle(),
                next == null ? null : next.getScheduledFor()
        );
    }

    private GameDashboardResponse.ReleaseReadinessSummary releaseReadinessSummary(Long gameId) {
        ReleaseReadinessResponse readiness = releaseChecklistService.calculateReadiness(gameId);
        return new GameDashboardResponse.ReleaseReadinessSummary(
                readiness.totalItems(),
                readiness.completedItems(),
                readiness.readinessPercentage(),
                readiness.blocked(),
                readiness.blockingItems()
        );
    }
}

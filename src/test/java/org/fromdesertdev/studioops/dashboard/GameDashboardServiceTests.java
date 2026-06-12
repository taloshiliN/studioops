package org.fromdesertdev.studioops.dashboard;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.game.GameStage;
import org.fromdesertdev.studioops.game.ValidationStatus;
import org.fromdesertdev.studioops.marketing.MarketingActivity;
import org.fromdesertdev.studioops.marketing.MarketingActivityRepository;
import org.fromdesertdev.studioops.marketing.MarketingActivityType;
import org.fromdesertdev.studioops.milestone.Milestone;
import org.fromdesertdev.studioops.milestone.MilestoneRepository;
import org.fromdesertdev.studioops.milestone.MilestoneStatus;
import org.fromdesertdev.studioops.workitem.WorkItem;
import org.fromdesertdev.studioops.workitem.WorkItemRepository;
import org.fromdesertdev.studioops.workitem.WorkItemStatus;
import org.fromdesertdev.studioops.playtest.Playtest;
import org.fromdesertdev.studioops.playtest.PlaytestRepository;
import org.fromdesertdev.studioops.releasechecklist.ReleaseChecklistService;
import org.fromdesertdev.studioops.releasechecklist.ReleaseReadinessResponse;
import org.fromdesertdev.studioops.traction.TractionSnapshot;
import org.fromdesertdev.studioops.traction.TractionSnapshotRepository;
import org.fromdesertdev.studioops.traction.TractionSource;
import org.fromdesertdev.studioops.validation.ValidationDecision;
import org.fromdesertdev.studioops.validation.ValidationDecisionRepository;
import org.fromdesertdev.studioops.validation.ValidationDecisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameDashboardServiceTests {
    private GameRepository gameRepository;
    private ValidationDecisionRepository validationDecisionRepository;
    private TractionSnapshotRepository tractionSnapshotRepository;
    private MilestoneRepository milestoneRepository;
    private WorkItemRepository workItemRepository;
    private PlaytestRepository playtestRepository;
    private MarketingActivityRepository marketingActivityRepository;
    private ReleaseChecklistService releaseChecklistService;
    private PermissionService permissionService;
    private GameDashboardService service;

    @BeforeEach
    void setUp() {
        gameRepository = mock(GameRepository.class);
        validationDecisionRepository = mock(ValidationDecisionRepository.class);
        tractionSnapshotRepository = mock(TractionSnapshotRepository.class);
        milestoneRepository = mock(MilestoneRepository.class);
        workItemRepository = mock(WorkItemRepository.class);
        playtestRepository = mock(PlaytestRepository.class);
        marketingActivityRepository = mock(MarketingActivityRepository.class);
        releaseChecklistService = mock(ReleaseChecklistService.class);
        permissionService = mock(PermissionService.class);
        service = new GameDashboardService(
                gameRepository,
                validationDecisionRepository,
                tractionSnapshotRepository,
                milestoneRepository,
                workItemRepository,
                playtestRepository,
                marketingActivityRepository,
                releaseChecklistService,
                permissionService
        );
    }

    @Test
    void combinesGameBusinessSignals() {
        Game game = mock(Game.class);
        when(game.getId()).thenReturn(1L);
        when(game.getTitle()).thenReturn("Sandstorm Courier");
        when(game.getCurrentStage()).thenReturn(GameStage.MARKETING);
        when(game.getValidationStatus()).thenReturn(ValidationStatus.VALIDATED);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        ValidationDecision decision = mock(ValidationDecision.class);
        when(decision.getDecision()).thenReturn(ValidationDecisionType.GREENLIGHT);
        when(decision.getReason()).thenReturn("Strong prototype traction");
        when(decision.getDecidedAt()).thenReturn(LocalDateTime.of(2026, 6, 6, 10, 0));
        when(validationDecisionRepository.findByGame_IdOrderByDecidedAtDesc(1L)).thenReturn(List.of(decision));

        TractionSnapshot traction = mock(TractionSnapshot.class);
        when(traction.getSource()).thenReturn(TractionSource.ITCH_IO);
        when(traction.getViews()).thenReturn(500);
        when(traction.getDownloads()).thenReturn(120);
        when(traction.getPlays()).thenReturn(150);
        when(traction.getRatingsCount()).thenReturn(20);
        when(traction.getAverageRating()).thenReturn(new BigDecimal("4.50"));
        when(traction.getCapturedAt()).thenReturn(LocalDateTime.of(2026, 6, 7, 10, 0));
        when(tractionSnapshotRepository.findByGame_IdOrderByCapturedAtDesc(1L)).thenReturn(List.of(traction));

        Milestone completedMilestone = mock(Milestone.class);
        Milestone blockedMilestone = mock(Milestone.class);
        when(completedMilestone.getStatus()).thenReturn(MilestoneStatus.COMPLETED);
        when(blockedMilestone.getStatus()).thenReturn(MilestoneStatus.BLOCKED);
        when(milestoneRepository.findByGame_IdOrderByDueDateAsc(1L))
                .thenReturn(List.of(completedMilestone, blockedMilestone));

        Playtest playtest = mock(Playtest.class);
        when(playtest.getSessionDate()).thenReturn(LocalDate.of(2026, 9, 12));
        when(playtest.getBuildVersion()).thenReturn("0.5.0-alpha");
        when(playtest.getMainFindings()).thenReturn("Tutorial was unclear");
        when(playtestRepository.findByGame_IdOrderBySessionDateDesc(1L)).thenReturn(List.of(playtest));

        MarketingActivity completedActivity = mock(MarketingActivity.class);
        MarketingActivity nextActivity = mock(MarketingActivity.class);
        when(completedActivity.getCompletedAt()).thenReturn(LocalDateTime.of(2026, 10, 2, 12, 0));
        when(nextActivity.getActivityType()).thenReturn(MarketingActivityType.TRAILER);
        when(nextActivity.getChannel()).thenReturn("YouTube");
        when(nextActivity.getTitle()).thenReturn("Gameplay trailer");
        when(nextActivity.getScheduledFor()).thenReturn(LocalDateTime.of(2026, 10, 10, 10, 0));
        when(marketingActivityRepository.findByGame_IdOrderByScheduledForAscCreatedAtAsc(1L))
                .thenReturn(List.of(completedActivity, nextActivity));

        when(releaseChecklistService.calculateReadiness(1L))
                .thenReturn(new ReleaseReadinessResponse(1L, 2, 1, 50, true, List.of("Critical bugs resolved")));

        WorkItem todoItem = mock(WorkItem.class);
        WorkItem blockedItem = mock(WorkItem.class);
        WorkItem doneItem = mock(WorkItem.class);

        when(todoItem.getStatus()).thenReturn(WorkItemStatus.TODO);
        when(todoItem.getDueDate()).thenReturn(LocalDate.now().minusDays(1));

        when(blockedItem.getStatus()).thenReturn(WorkItemStatus.BLOCKED);
        when(blockedItem.getDueDate()).thenReturn(LocalDate.now().plusDays(2));

        when(doneItem.getStatus()).thenReturn(WorkItemStatus.DONE);
        when(doneItem.getDueDate()).thenReturn(LocalDate.now().minusDays(3));

        when(workItemRepository.findByGame_IdOrderByDueDateAscCreatedAtAsc(1L))
                .thenReturn(List.of(todoItem, blockedItem, doneItem));


        GameDashboardResponse response = service.getDashboard(1L);

        assertEquals("Sandstorm Courier", response.game().title());
        assertEquals(ValidationDecisionType.GREENLIGHT, response.validation().latestDecision());
        assertEquals(500, response.traction().views());
        assertEquals(2, response.milestones().total());
        assertEquals(1, response.milestones().completed());
        assertEquals(1, response.milestones().blocked());
        assertEquals("Tutorial was unclear", response.playtests().latestMainFindings());
        assertEquals(2, response.marketing().total());
        assertEquals(1, response.marketing().completed());
        assertEquals("Gameplay trailer", response.marketing().nextTitle());
        assertEquals(50, response.releaseReadiness().readinessPercentage());
        assertTrue(response.releaseReadiness().blocked());

        assertEquals(3, response.workItems().total());
        assertEquals(1, response.workItems().todo());
        assertEquals(1, response.workItems().blocked());
        assertEquals(1, response.workItems().done());
        assertEquals(1, response.workItems().overdue());
    }
}

package org.fromdesertdev.studioops.dashboard;

import org.fromdesertdev.studioops.game.GameStage;
import org.fromdesertdev.studioops.game.ValidationStatus;
import org.fromdesertdev.studioops.marketing.MarketingActivityType;
import org.fromdesertdev.studioops.traction.TractionSource;
import org.fromdesertdev.studioops.validation.ValidationDecisionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GameDashboardResponse(
        GameSummary game,
        ValidationSummary validation,
        TractionSummary traction,
        MilestoneSummary milestones,
        WorkItemSummary workItems,
        PlaytestSummary playtests,
        MarketingSummary marketing,
        LaunchPlanSummary launchPlan,
        ReleaseReadinessSummary releaseReadiness
) {
    public record GameSummary(
            Long id,
            String title,
            GameStage currentStage,
            ValidationStatus validationStatus
    ) {
    }

    public record ValidationSummary(
            ValidationDecisionType latestDecision,
            String reason,
            LocalDateTime decidedAt
    ) {
    }

    public record TractionSummary(
            TractionSource latestSource,
            int views,
            int downloads,
            int plays,
            int ratingsCount,
            BigDecimal averageRating,
            int commentsCount,
            int followersGained,
            int wishlists,
            int revenueCents,
            LocalDateTime capturedAt
    ) {
    }

    public record MilestoneSummary(
            int total,
            int completed,
            int inProgress,
            int blocked
    ) {
    }

    public record WorkItemSummary(
            int total,
            int todo,
            int inProgress,
            int blocked,
            int done,
            int overdue
    ){

    }

    public record PlaytestSummary(
            int total,
            LocalDate latestSessionDate,
            String latestBuildVersion,
            String latestMainFindings
    ) {
    }

    public record MarketingSummary(
            int total,
            int completed,
            int upcoming,
            MarketingActivityType nextActivityType,
            String nextChannel,
            String nextTitle,
            LocalDateTime nextScheduledFor
    ) {
    }

    public record LaunchPlanSummary(
            String itchPageUrl,
            String steamPageUrl,
            String demoUrl,
            String trailerUrl,
            LocalDate targetDemoDate,
            LocalDate targetNextFestDate,
            LocalDate targetLaunchDate,
            int contentCreatorOutreachTarget,
            int festivalSubmissionTarget,
            int readinessPercentage,
            List<String> missingItems
    ) {
    }

    public record ReleaseReadinessSummary(
            int totalItems,
            int completedItems,
            int readinessPercentage,
            boolean blocked,
            List<String> blockingItems
    ) {
    }
}

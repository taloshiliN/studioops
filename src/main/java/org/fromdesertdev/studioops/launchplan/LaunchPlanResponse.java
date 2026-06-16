package org.fromdesertdev.studioops.launchplan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record LaunchPlanResponse(
        Long id,
        Long gameId,
        String itchPageUrl,
        String steamPageUrl,
        String demoUrl,
        String trailerUrl,
        LocalDate targetDemoDate,
        LocalDate targetNextFestDate,
        LocalDate targetLaunchDate,
        Integer contentCreatorOutreachTarget,
        Integer festivalSubmissionTarget,
        String notes,
        int readinessPercentage,
        List<String> missingItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final int TOTAL_CHECKS = 8;

    public static LaunchPlanResponse from(LaunchPlan plan) {
        List<String> missingItems = missingItems(
                plan.getItchPageUrl(),
                plan.getSteamPageUrl(),
                plan.getDemoUrl(),
                plan.getTrailerUrl(),
                plan.getTargetDemoDate(),
                plan.getTargetNextFestDate(),
                plan.getTargetLaunchDate(),
                plan.getContentCreatorOutreachTarget(),
                plan.getFestivalSubmissionTarget()
        );

        return new LaunchPlanResponse(
                plan.getId(),
                plan.getGame().getId(),
                plan.getItchPageUrl(),
                plan.getSteamPageUrl(),
                plan.getDemoUrl(),
                plan.getTrailerUrl(),
                plan.getTargetDemoDate(),
                plan.getTargetNextFestDate(),
                plan.getTargetLaunchDate(),
                plan.getContentCreatorOutreachTarget(),
                plan.getFestivalSubmissionTarget(),
                plan.getNotes(),
                readinessPercentage(missingItems),
                missingItems,
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    public static LaunchPlanResponse empty(Long gameId) {
        return new LaunchPlanResponse(
                null,
                gameId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LaunchPlan.DEFAULT_CONTENT_CREATOR_TARGET,
                LaunchPlan.DEFAULT_FESTIVAL_TARGET,
                null,
                0,
                List.of(
                        "Publish an itch.io prototype page",
                        "Create the Steam page",
                        "Publish a playable demo",
                        "Prepare the trailer",
                        "Choose the demo target date",
                        "Choose the Next Fest target date",
                        "Choose the launch target date",
                        "Save the outreach and festival targets"
                ),
                null,
                null
        );
    }

    private static List<String> missingItems(
            String itchPageUrl,
            String steamPageUrl,
            String demoUrl,
            String trailerUrl,
            LocalDate targetDemoDate,
            LocalDate targetNextFestDate,
            LocalDate targetLaunchDate,
            Integer contentCreatorOutreachTarget,
            Integer festivalSubmissionTarget
    ) {
        List<String> items = new ArrayList<>();

        if (isBlank(itchPageUrl)) {
            items.add("Publish an itch.io prototype page");
        }

        if (isBlank(steamPageUrl)) {
            items.add("Create the Steam page");
        }

        if (isBlank(demoUrl)) {
            items.add("Publish a playable demo");
        }

        if (isBlank(trailerUrl)) {
            items.add("Prepare the trailer");
        }

        if (targetDemoDate == null) {
            items.add("Choose the demo target date");
        }

        if (targetNextFestDate == null) {
            items.add("Choose the Next Fest target date");
        }

        if (targetLaunchDate == null) {
            items.add("Choose the launch target date");
        }

        if (contentCreatorOutreachTarget == null || contentCreatorOutreachTarget <= 0
                || festivalSubmissionTarget == null || festivalSubmissionTarget <= 0) {
            items.add("Save the outreach and festival targets");
        }

        return items;
    }

    private static int readinessPercentage(List<String> missingItems) {
        int completedChecks = TOTAL_CHECKS - missingItems.size();
        return Math.max(0, completedChecks * 100 / TOTAL_CHECKS);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

package org.fromdesertdev.studioops.launchplan;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpsertLaunchPlanRequest(
        @Size(max = 500) String itchPageUrl,
        @Size(max = 500) String steamPageUrl,
        @Size(max = 500) String demoUrl,
        @Size(max = 500) String trailerUrl,
        LocalDate targetDemoDate,
        LocalDate targetNextFestDate,
        LocalDate targetLaunchDate,
        @PositiveOrZero Integer contentCreatorOutreachTarget,
        @PositiveOrZero Integer festivalSubmissionTarget,
        @Size(max = 5000) String notes
) {
}

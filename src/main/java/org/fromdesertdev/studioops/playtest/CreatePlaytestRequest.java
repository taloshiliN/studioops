package org.fromdesertdev.studioops.playtest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePlaytestRequest(
        @NotNull LocalDate sessionDate,
        @Size(max = 160) String testerGroup,
        @Size(max = 80) String buildVersion,
        @Size(max = 5000) String notes,
        @Size(max = 5000) String mainFindings
) {
}

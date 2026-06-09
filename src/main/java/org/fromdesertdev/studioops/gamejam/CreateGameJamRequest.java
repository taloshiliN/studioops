package org.fromdesertdev.studioops.gamejam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateGameJamRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 160) String host,
        @Size(max = 160) String theme,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 500) String url
) {
}

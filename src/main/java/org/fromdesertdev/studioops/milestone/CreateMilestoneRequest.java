package org.fromdesertdev.studioops.milestone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateMilestoneRequest(
        @NotBlank @Size(max = 120) String name,
        LocalDate dueDate
) {
}

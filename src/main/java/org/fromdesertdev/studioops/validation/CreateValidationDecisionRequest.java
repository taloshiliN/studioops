package org.fromdesertdev.studioops.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateValidationDecisionRequest(
        @NotNull ValidationDecisionType decision,
        @NotBlank @Size(max = 2000) String reason
) {
}

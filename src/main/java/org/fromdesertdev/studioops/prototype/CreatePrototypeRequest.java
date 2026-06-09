package org.fromdesertdev.studioops.prototype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePrototypeRequest(
        @NotNull Long gameId,
        Long gameJamId,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 80) String buildVersion,
        @Size(max = 500) String itchUrl,
        @Size(max = 500) String repositoryUrl,
        @Size(max = 500) String playableUrl
) {
}

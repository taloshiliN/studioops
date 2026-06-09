package org.fromdesertdev.studioops.releasechecklist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReleaseChecklistItemRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 2000) String description,
        boolean blocksRelease
) {
}

package org.fromdesertdev.studioops.studio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStudioRequest(
        @NotBlank
        @Size(max = 120)
        String name
) {

}

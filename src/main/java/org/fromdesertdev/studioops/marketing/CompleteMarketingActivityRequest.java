package org.fromdesertdev.studioops.marketing;

import jakarta.validation.constraints.Size;

public record CompleteMarketingActivityRequest(
        @Size(max = 5000) String resultNotes
) {
}

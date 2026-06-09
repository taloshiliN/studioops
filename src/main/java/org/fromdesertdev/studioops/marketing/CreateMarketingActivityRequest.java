package org.fromdesertdev.studioops.marketing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateMarketingActivityRequest(
        @NotNull MarketingActivityType activityType,
        @NotBlank @Size(max = 80) String channel,
        @NotBlank @Size(max = 160) String title,
        LocalDateTime scheduledFor
) {
}

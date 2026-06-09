package org.fromdesertdev.studioops.marketing;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MarketingActivityController {
    private final MarketingActivityService marketingActivityService;

    public MarketingActivityController(MarketingActivityService marketingActivityService) {
        this.marketingActivityService = marketingActivityService;
    }

    @PostMapping("/games/{gameId}/marketing-activities")
    @ResponseStatus(HttpStatus.CREATED)
    public MarketingActivityResponse create(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateMarketingActivityRequest request
    ) {
        return marketingActivityService.create(gameId, request);
    }

    @GetMapping("/games/{gameId}/marketing-activities")
    public List<MarketingActivityResponse> findByGame(@PathVariable Long gameId) {
        return marketingActivityService.findByGame(gameId);
    }

    @GetMapping("/marketing-activities/{id}")
    public MarketingActivityResponse findById(@PathVariable Long id) {
        return marketingActivityService.findById(id);
    }

    @PatchMapping("/marketing-activities/{id}/complete")
    public MarketingActivityResponse complete(
            @PathVariable Long id,
            @Valid @RequestBody CompleteMarketingActivityRequest request
    ) {
        return marketingActivityService.complete(id, request);
    }
}

package org.fromdesertdev.studioops.releasechecklist;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReleaseChecklistController {
    private final ReleaseChecklistService releaseChecklistService;

    public ReleaseChecklistController(ReleaseChecklistService releaseChecklistService) {
        this.releaseChecklistService = releaseChecklistService;
    }

    @PostMapping("/games/{gameId}/release-checklist")
    @ResponseStatus(HttpStatus.CREATED)
    public ReleaseChecklistItemResponse create(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateReleaseChecklistItemRequest request
    ) {
        return releaseChecklistService.create(gameId, request);
    }

    @GetMapping("/games/{gameId}/release-checklist")
    public List<ReleaseChecklistItemResponse> findByGame(@PathVariable Long gameId) {
        return releaseChecklistService.findByGame(gameId);
    }

    @PatchMapping("/release-checklist/{itemId}/completion")
    public ReleaseChecklistItemResponse updateCompletion(
            @PathVariable Long itemId,
            @RequestBody UpdateChecklistCompletionRequest request
    ) {
        return releaseChecklistService.updateCompletion(itemId, request);
    }

    @GetMapping("/games/{gameId}/release-readiness")
    public ReleaseReadinessResponse calculateReadiness(@PathVariable Long gameId) {
        return releaseChecklistService.calculateReadiness(gameId);
    }
}

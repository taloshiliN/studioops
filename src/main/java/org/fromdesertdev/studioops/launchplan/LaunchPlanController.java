package org.fromdesertdev.studioops.launchplan;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LaunchPlanController {
    private final LaunchPlanService launchPlanService;

    public LaunchPlanController(LaunchPlanService launchPlanService) {
        this.launchPlanService = launchPlanService;
    }

    @GetMapping("/games/{gameId}/launch-plan")
    public LaunchPlanResponse findByGame(@PathVariable Long gameId) {
        return launchPlanService.findByGame(gameId);
    }

    @PutMapping("/games/{gameId}/launch-plan")
    public LaunchPlanResponse upsert(
            @PathVariable Long gameId,
            @Valid @RequestBody UpsertLaunchPlanRequest request
    ) {
        return launchPlanService.upsert(gameId, request);
    }
}

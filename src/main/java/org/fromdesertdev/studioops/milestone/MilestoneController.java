package org.fromdesertdev.studioops.milestone;

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
public class MilestoneController {
    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @PostMapping("/games/{gameId}/milestones")
    @ResponseStatus(HttpStatus.CREATED)
    public MilestoneResponse create(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateMilestoneRequest request
    ) {
        return milestoneService.create(gameId, request);
    }

    @GetMapping("/games/{gameId}/milestones")
    public List<MilestoneResponse> findByGame(@PathVariable Long gameId) {
        return milestoneService.findByGame(gameId);
    }

    @PatchMapping("/milestones/{milestoneId}/status")
    public MilestoneResponse updateStatus(
            @PathVariable Long milestoneId,
            @Valid @RequestBody UpdateMilestoneStatusRequest request
    ) {
        return milestoneService.updateStatus(milestoneId, request);
    }
}

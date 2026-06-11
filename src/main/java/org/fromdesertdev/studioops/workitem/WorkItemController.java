package org.fromdesertdev.studioops.workitem;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WorkItemController {
    private final WorkItemService workItemService;

    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    @PostMapping("/games/{gameId}/work-items")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkItemResponse create(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateWorkItemRequest request
    ) {
        return workItemService.create(gameId, request);
    }

    @GetMapping("/games/{gameId}/work-items")
    public List<WorkItemResponse> findByGame(
            @PathVariable Long gameId
    ) {
        return workItemService.findByGame(gameId);
    }

    @GetMapping("/work-items/{workItemId}")
    public WorkItemResponse findById(
            @PathVariable Long workItemId
    ) {
        return workItemService.findById(workItemId);
    }

    @PatchMapping("/work-items/{workItemId}/status")
    public WorkItemResponse updateStatus(
            @PathVariable Long workItemId,
            @Valid @RequestBody UpdateWorkItemStatusRequest request
    ) {
        return workItemService.updateStatus(workItemId, request);
    }

    @PatchMapping("/work-items/{workItemId}/assignee")
    public WorkItemResponse assign(
            @PathVariable Long workItemId,
            @Valid @RequestBody AssignWorkItemRequest request
    ) {
        return workItemService.assign(workItemId, request);
    }
}
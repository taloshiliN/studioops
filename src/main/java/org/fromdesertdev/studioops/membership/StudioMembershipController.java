package org.fromdesertdev.studioops.membership;

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
@RequestMapping("/api/studios/{studioId}/members")
public class StudioMembershipController {
    private final StudioMembershipService studioMembershipService;

    public StudioMembershipController(StudioMembershipService studioMembershipService) {
        this.studioMembershipService = studioMembershipService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudioMembershipResponse addMember(
            @PathVariable Long studioId,
            @Valid @RequestBody AddStudioMemberRequest request
    ) {
        return studioMembershipService.addMember(studioId, request);
    }

    @GetMapping
    public List<StudioMembershipResponse> findByStudio(@PathVariable Long studioId) {
        return studioMembershipService.findByStudio(studioId);
    }

    @PatchMapping("/{userId}/role")
    public StudioMembershipResponse updateRole(
            @PathVariable Long studioId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateStudioMemberRoleRequest request
    ) {
        return studioMembershipService.updateRole(studioId, userId, request);
    }
}

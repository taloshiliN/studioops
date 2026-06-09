package org.fromdesertdev.studioops.membership;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.studio.Studio;
import org.fromdesertdev.studioops.studio.StudioRepository;
import org.fromdesertdev.studioops.user.AppUser;
import org.fromdesertdev.studioops.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudioMembershipService {
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;
    private final AppUserRepository appUserRepository;
    private final PermissionService permissionService;

    public StudioMembershipService(
            StudioMembershipRepository studioMembershipRepository,
            StudioRepository studioRepository,
            AppUserRepository appUserRepository,
            PermissionService permissionService
    ) {
        this.studioMembershipRepository = studioMembershipRepository;
        this.studioRepository = studioRepository;
        this.appUserRepository = appUserRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public StudioMembershipResponse addMember(Long studioId, AddStudioMemberRequest request) {
        permissionService.requireStudioRole(
                studioId,
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        Studio studio = findStudio(studioId);
        AppUser user = appUserRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (studioMembershipRepository.existsByStudio_IdAndUser_Id(studioId, request.userId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a studio member");
        }

        StudioMembership membership = new StudioMembership(studio, user, request.role());

        return StudioMembershipResponse.from(studioMembershipRepository.save(membership));
    }

    @Transactional(readOnly = true)
    public List<StudioMembershipResponse> findByStudio(Long studioId) {
        permissionService.requireStudioMember(studioId);
        findStudio(studioId);

        return studioMembershipRepository.findByStudio_IdOrderByCreatedAtAsc(studioId)
                .stream()
                .map(StudioMembershipResponse::from)
                .toList();
    }

    @Transactional
    public StudioMembershipResponse updateRole(
            Long studioId,
            Long userId,
            UpdateStudioMemberRoleRequest request
    ) {
        permissionService.requireStudioRole(studioId, MembershipRole.OWNER);

        StudioMembership membership = studioMembershipRepository.findByStudio_IdAndUser_Id(studioId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studio member not found"));

        membership.changeRole(request.role());
        return StudioMembershipResponse.from(membership);
    }

    private Studio findStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studio not found"));
    }
}

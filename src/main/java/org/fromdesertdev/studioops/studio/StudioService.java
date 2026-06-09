package org.fromdesertdev.studioops.studio;

import org.fromdesertdev.studioops.membership.MembershipRole;
import org.fromdesertdev.studioops.membership.StudioMembership;
import org.fromdesertdev.studioops.membership.StudioMembershipRepository;
import org.fromdesertdev.studioops.user.AppUser;
import org.fromdesertdev.studioops.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudioService {
    private final StudioRepository studioRepository;
    private final AppUserRepository appUserRepository;
    private final StudioMembershipRepository studioMembershipRepository;

    public StudioService(
            StudioRepository studioRepository,
            AppUserRepository appUserRepository,
            StudioMembershipRepository studioMembershipRepository
    ){
        this.studioRepository = studioRepository;
        this.appUserRepository = appUserRepository;
        this.studioMembershipRepository = studioMembershipRepository;
    }

    @Transactional
    public StudioResponse create(CreateStudioRequest request, String ownerEmail){
        AppUser owner = appUserRepository.findByEmail(normalizeEmail(ownerEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Studio studio = new Studio(request.name());
        Studio savedStudio = studioRepository.save(studio);
        studioMembershipRepository.save(new StudioMembership(savedStudio, owner, MembershipRole.OWNER));

        return StudioResponse.from(savedStudio);
    }

    @Transactional(readOnly = true)
    public List<StudioResponse> findAll() {
        return studioRepository.findAll()
                .stream()
                .map(StudioResponse::from)
                .toList();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}

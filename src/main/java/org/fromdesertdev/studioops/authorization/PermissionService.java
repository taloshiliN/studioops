package org.fromdesertdev.studioops.authorization;

import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.fromdesertdev.studioops.membership.StudioMembership;
import org.fromdesertdev.studioops.membership.StudioMembershipRepository;
import org.fromdesertdev.studioops.user.AppUser;
import org.fromdesertdev.studioops.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
public class PermissionService {
    private final AppUserRepository appUserRepository;
    private final StudioMembershipRepository membershipRepository;
    private final GameRepository gameRepository;

    public PermissionService(
            AppUserRepository appUserRepository,
            StudioMembershipRepository membershipRepository,
            GameRepository gameRepository
    ){
        this.appUserRepository = appUserRepository;
        this.membershipRepository = membershipRepository;
        this.gameRepository = gameRepository;
    }

    public AppUser requireCurrentUser(){
        String email = currentUserEmail();

        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public void requireStudioMember(Long studioId){
        AppUser user = requireCurrentUser();

        if (!membershipRepository.existsByStudio_IdAndUser_Id(studioId, user.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this studio");
        }
    }

    public void requireStudioRole(Long studioId, MembershipRole... allowedRoles){
        AppUser user = requireCurrentUser();

        StudioMembership membership = membershipRepository.findByStudio_IdAndUser_Id(studioId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this studio"));

        boolean allowed = Arrays.asList(allowedRoles).contains(membership.getRole());

        if (!allowed){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission for this action");
        }
    }

    public void requireGameMember(Long gameId){
        Game game = findGame(gameId);
        requireStudioMember(game.getStudio().getId());
    }

    public void requireGameRole(Long gameId, MembershipRole... allowedRoles){
        Game game = findGame(gameId);
        requireStudioRole(game.getStudio().getId(), allowedRoles);
    }

    private Game findGame(Long gameId){
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return authentication.getName().trim().toLowerCase();
    }
}

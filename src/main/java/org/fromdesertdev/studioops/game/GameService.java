package org.fromdesertdev.studioops.game;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.fromdesertdev.studioops.membership.StudioMembershipRepository;
import org.fromdesertdev.studioops.studio.Studio;
import org.fromdesertdev.studioops.studio.StudioRepository;
import org.fromdesertdev.studioops.user.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final StudioRepository studioRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final PermissionService permissionService;

    public GameService(
            GameRepository gameRepository,
            StudioRepository studioRepository,
            StudioMembershipRepository studioMembershipRepository,
            PermissionService permissionService
    ) {
        this.gameRepository = gameRepository;
        this.studioRepository = studioRepository;
        this.studioMembershipRepository = studioMembershipRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public GameResponse create(CreateGameRequest request) {
        Studio studio = studioRepository.findById(request.studioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studio not found"));

        permissionService.requireStudioRole(
                request.studioId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        Game game = new Game(
                studio,
                request.title(),
                request.shortPitch(),
                request.genre(),
                request.targetPlatforms(),
                request.fontFamily()
        );

        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findAll() {
        AppUser user = permissionService.requireCurrentUser();

        List<Long> studioIds = studioMembershipRepository.findByUser_Id(user.getId())
                .stream()
                .map(membership -> membership.getStudio().getId())
                .toList();

        return gameRepository.findByStudio_IdInOrderByCreatedAtDesc(studioIds)
                .stream()
                .map(GameResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameResponse findById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        permissionService.requireGameMember(id);

        return GameResponse.from(game);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findByStudio(Long studioId) {
        permissionService.requireStudioMember(studioId);

        return gameRepository.findByStudio_Id(studioId)
                .stream()
                .map(GameResponse::from)
                .toList();
    }
}

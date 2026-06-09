package org.fromdesertdev.studioops.prototype;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.gamejam.GameJam;
import org.fromdesertdev.studioops.gamejam.GameJamRepository;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.fromdesertdev.studioops.membership.StudioMembership;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PrototypeService {
    private final PrototypeRepository prototypeRepository;
    private final GameRepository gameRepository;
    private final GameJamRepository gameJamRepository;
    private final PermissionService permissionService;

    public PrototypeService(
            PrototypeRepository prototypeRepository,
            GameRepository gameRepository,
            GameJamRepository gameJamRepository,
            PermissionService permissionService
    ) {
        this.prototypeRepository = prototypeRepository;
        this.gameRepository = gameRepository;
        this.gameJamRepository = gameJamRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public PrototypeResponse create(CreatePrototypeRequest request) {
        permissionService.requireGameRole(
                request.gameId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );
        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        GameJam gameJam = null;
        if (request.gameJamId() != null) {
            gameJam = gameJamRepository.findById(request.gameJamId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game jam not found"));
        }

        Prototype prototype = new Prototype(
                game,
                gameJam,
                request.name(),
                request.buildVersion(),
                request.itchUrl(),
                request.repositoryUrl(),
                request.playableUrl()
        );

        return PrototypeResponse.from(prototypeRepository.save(prototype));
    }

    @Transactional(readOnly = true)
    public PrototypeResponse findById(Long id) {
        Prototype prototype = prototypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prototype not found"));

        permissionService.requireGameMember(prototype.getGame().getId());

        return PrototypeResponse.from(prototype);
    }

    @Transactional(readOnly = true)
    public List<PrototypeResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);

        return prototypeRepository.findByGame_Id(gameId)
                .stream()
                .map(PrototypeResponse::from)
                .toList();
    }

//    private MembershipRole findMember(Long id){
//        return StudioMembership
//    }
}

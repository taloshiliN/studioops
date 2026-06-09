package org.fromdesertdev.studioops.traction;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.fromdesertdev.studioops.prototype.Prototype;
import org.fromdesertdev.studioops.prototype.PrototypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TractionSnapshotService {
    private final TractionSnapshotRepository tractionSnapshotRepository;
    private final GameRepository gameRepository;
    private final PrototypeRepository prototypeRepository;
    private final PermissionService permissionService;

    public TractionSnapshotService(
            TractionSnapshotRepository tractionSnapshotRepository,
            GameRepository gameRepository,
            PrototypeRepository prototypeRepository,
            PermissionService permissionService
    ) {
        this.tractionSnapshotRepository = tractionSnapshotRepository;
        this.gameRepository = gameRepository;
        this.prototypeRepository = prototypeRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public TractionSnapshotResponse create(CreateTractionSnapshotRequest request) {
        permissionService.requireGameRole(
                request.gameId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );

        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        Prototype prototype = null;
        if (request.prototypeId() != null) {
            prototype = prototypeRepository.findById(request.prototypeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prototype not found"));

            if (!prototype.getGame().getId().equals(game.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Prototype does not belong to the selected game"
                );
            }
        }

        TractionSnapshot snapshot = new TractionSnapshot(
                game,
                prototype,
                request.source(),
                request.views(),
                request.downloads(),
                request.plays(),
                request.ratingsCount(),
                request.averageRating(),
                request.commentsCount(),
                request.followersGained(),
                request.wishlists(),
                request.revenueCents(),
                request.capturedAt()
        );

        return TractionSnapshotResponse.from(tractionSnapshotRepository.save(snapshot));
    }

    @Transactional(readOnly = true)
    public List<TractionSnapshotResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);
        return tractionSnapshotRepository.findByGame_IdOrderByCapturedAtAsc(gameId)
                .stream()
                .map(TractionSnapshotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TractionSnapshotResponse> findByPrototype(Long prototypeId) {
        Prototype prototype = prototypeRepository.findById(prototypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prototype not found"));

        permissionService.requireGameMember(prototype.getGame().getId());

        return tractionSnapshotRepository.findByPrototype_IdOrderByCapturedAtAsc(prototypeId)
                .stream()
                .map(TractionSnapshotResponse::from)
                .toList();
    }
}

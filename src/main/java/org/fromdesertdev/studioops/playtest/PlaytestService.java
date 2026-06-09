package org.fromdesertdev.studioops.playtest;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.game.GameStage;
import org.fromdesertdev.studioops.game.ValidationStatus;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlaytestService {
    private final PlaytestRepository playtestRepository;
    private final GameRepository gameRepository;
    private final PermissionService permissionService;

    public PlaytestService(
            PlaytestRepository playtestRepository,
            GameRepository gameRepository,
            PermissionService permissionService
    ) {
        this.playtestRepository = playtestRepository;
        this.gameRepository = gameRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public PlaytestResponse create(Long gameId, CreatePlaytestRequest request) {
        permissionService.requireGameRole(
                gameId,
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );

        Game game = findGame(gameId);

        if (game.getValidationStatus() != ValidationStatus.VALIDATED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Playtests can only be recorded for a validated game"
            );
        }

        Playtest playtest = new Playtest(
                game,
                request.sessionDate(),
                request.testerGroup(),
                request.buildVersion(),
                request.notes(),
                request.mainFindings()
        );

        game.moveToStage(GameStage.PLAYTESTING);

        return PlaytestResponse.from(playtestRepository.save(playtest));
    }

    @Transactional(readOnly = true)
    public List<PlaytestResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);
        findGame(gameId);

        return playtestRepository.findByGame_IdOrderBySessionDateDesc(gameId)
                .stream()
                .map(PlaytestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaytestResponse findById(Long id) {
        Playtest playtest = playtestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playtest not found"));

        permissionService.requireGameMember(playtest.getGame().getId());

        return PlaytestResponse.from(playtest);
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }
}

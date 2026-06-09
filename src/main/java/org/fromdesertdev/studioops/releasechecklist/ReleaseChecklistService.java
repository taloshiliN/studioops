package org.fromdesertdev.studioops.releasechecklist;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.game.ValidationStatus;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReleaseChecklistService {
    private final ReleaseChecklistRepository releaseChecklistRepository;
    private final GameRepository gameRepository;
    private final PermissionService permissionService;

    public ReleaseChecklistService(
            ReleaseChecklistRepository releaseChecklistRepository,
            GameRepository gameRepository,
            PermissionService permissionService
    ) {
        this.releaseChecklistRepository = releaseChecklistRepository;
        this.gameRepository = gameRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public ReleaseChecklistItemResponse create(Long gameId, CreateReleaseChecklistItemRequest request) {
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
                    "Release checklist items can only be created for a validated game"
            );
        }

        ReleaseChecklistItem item = new ReleaseChecklistItem(
                game,
                request.title(),
                request.description(),
                request.blocksRelease()
        );

        return ReleaseChecklistItemResponse.from(releaseChecklistRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ReleaseChecklistItemResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);
        findGame(gameId);

        return findItems(gameId)
                .stream()
                .map(ReleaseChecklistItemResponse::from)
                .toList();
    }

    @Transactional
    public ReleaseChecklistItemResponse updateCompletion(
            Long itemId,
            UpdateChecklistCompletionRequest request
    ) {
        ReleaseChecklistItem item = releaseChecklistRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Release checklist item not found"
                ));

        permissionService.requireGameRole(
                item.getGame().getId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );

        item.setCompleted(request.completed());
        return ReleaseChecklistItemResponse.from(item);
    }

    @Transactional(readOnly = true)
    public ReleaseReadinessResponse calculateReadiness(Long gameId) {
        permissionService.requireGameMember(gameId);
        findGame(gameId);

        List<ReleaseChecklistItem> items = findItems(gameId);

        if (items.isEmpty()) {
            return new ReleaseReadinessResponse(
                    gameId,
                    0,
                    0,
                    0,
                    true,
                    List.of("Release checklist has no items")
            );
        }

        int completedItems = (int) items.stream()
                .filter(ReleaseChecklistItem::isCompleted)
                .count();

        List<String> blockingItems = items.stream()
                .filter(item -> item.isBlocksRelease() && !item.isCompleted())
                .map(ReleaseChecklistItem::getTitle)
                .toList();

        int readinessPercentage = (int) Math.round(completedItems * 100.0 / items.size());

        return new ReleaseReadinessResponse(
                gameId,
                items.size(),
                completedItems,
                readinessPercentage,
                !blockingItems.isEmpty(),
                blockingItems
        );
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private List<ReleaseChecklistItem> findItems(Long gameId) {
        return releaseChecklistRepository.findByGame_IdOrderByCreatedAtAsc(gameId);
    }
}

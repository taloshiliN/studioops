package org.fromdesertdev.studioops.releasechecklist;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReleaseChecklistServiceTests {
    private ReleaseChecklistRepository releaseChecklistRepository;
    private GameRepository gameRepository;
    private PermissionService permissionService;
    private ReleaseChecklistService service;

    @BeforeEach
    void setUp() {
        releaseChecklistRepository = mock(ReleaseChecklistRepository.class);
        gameRepository = mock(GameRepository.class);
        permissionService = mock(PermissionService.class);
        service = new ReleaseChecklistService(releaseChecklistRepository, gameRepository, permissionService);
    }

    @Test
    void emptyChecklistIsNotReady() {
        Game game = mock(Game.class);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(releaseChecklistRepository.findByGame_IdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        ReleaseReadinessResponse response = service.calculateReadiness(1L);

        assertEquals(0, response.readinessPercentage());
        assertTrue(response.blocked());
        assertEquals(List.of("Release checklist has no items"), response.blockingItems());
    }

    @Test
    void readinessIncludesIncompleteBlockingItems() {
        Game game = mock(Game.class);
        ReleaseChecklistItem completed = mock(ReleaseChecklistItem.class);
        ReleaseChecklistItem blocker = mock(ReleaseChecklistItem.class);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(completed.isCompleted()).thenReturn(true);
        when(blocker.isCompleted()).thenReturn(false);
        when(blocker.isBlocksRelease()).thenReturn(true);
        when(blocker.getTitle()).thenReturn("Critical bugs resolved");
        when(releaseChecklistRepository.findByGame_IdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(completed, blocker));

        ReleaseReadinessResponse response = service.calculateReadiness(1L);

        assertEquals(50, response.readinessPercentage());
        assertTrue(response.blocked());
        assertEquals(List.of("Critical bugs resolved"), response.blockingItems());
    }

    @Test
    void completedChecklistIsReady() {
        Game game = mock(Game.class);
        ReleaseChecklistItem first = mock(ReleaseChecklistItem.class);
        ReleaseChecklistItem second = mock(ReleaseChecklistItem.class);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(first.isCompleted()).thenReturn(true);
        when(second.isCompleted()).thenReturn(true);
        when(releaseChecklistRepository.findByGame_IdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(first, second));

        ReleaseReadinessResponse response = service.calculateReadiness(1L);

        assertEquals(100, response.readinessPercentage());
        assertFalse(response.blocked());
        assertTrue(response.blockingItems().isEmpty());
    }
}

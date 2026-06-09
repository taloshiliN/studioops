package org.fromdesertdev.studioops.playtest;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaytestServiceTests {
    private PlaytestRepository playtestRepository;
    private GameRepository gameRepository;
    private PermissionService permissionService;
    private PlaytestService service;

    @BeforeEach
    void setUp() {
        playtestRepository = mock(PlaytestRepository.class);
        gameRepository = mock(GameRepository.class);
        permissionService = mock(PermissionService.class);
        service = new PlaytestService(playtestRepository, gameRepository, permissionService);
    }

    @Test
    void rejectsPlaytestForUnvalidatedGame() {
        Game game = mock(Game.class);
        when(game.getValidationStatus()).thenReturn(ValidationStatus.NEEDS_MORE_TESTING);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.create(1L, request()));

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void recordingPlaytestMovesGameToPlaytesting() {
        Game game = mock(Game.class);
        Playtest saved = mock(Playtest.class);
        when(game.getValidationStatus()).thenReturn(ValidationStatus.VALIDATED);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playtestRepository.save(any(Playtest.class))).thenReturn(saved);
        when(saved.getGame()).thenReturn(game);
        when(game.getId()).thenReturn(1L);

        service.create(1L, request());

        verify(game).moveToStage(GameStage.PLAYTESTING);
        verify(playtestRepository).save(any(Playtest.class));
    }

    private CreatePlaytestRequest request() {
        return new CreatePlaytestRequest(
                LocalDate.of(2026, 9, 12), "First-time strategy players",
                "0.5.0-alpha", "Five remote participants", "Tutorial was unclear");
    }
}

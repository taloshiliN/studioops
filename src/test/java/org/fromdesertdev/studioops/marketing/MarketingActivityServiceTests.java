package org.fromdesertdev.studioops.marketing;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.game.GameStage;
import org.fromdesertdev.studioops.game.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketingActivityServiceTests {
    private MarketingActivityRepository marketingActivityRepository;
    private GameRepository gameRepository;
    private PermissionService permissionService;
    private MarketingActivityService service;

    @BeforeEach
    void setUp() {
        marketingActivityRepository = mock(MarketingActivityRepository.class);
        gameRepository = mock(GameRepository.class);
        permissionService = mock(PermissionService.class);
        service = new MarketingActivityService(marketingActivityRepository, gameRepository, permissionService);
    }

    @Test
    void rejectsMarketingActivityForUnvalidatedGame() {
        Game game = mock(Game.class);
        when(game.getValidationStatus()).thenReturn(ValidationStatus.NEEDS_MORE_TESTING);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.create(1L, createRequest())
        );

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void creatingMarketingActivityMovesGameToMarketing() {
        Game game = mock(Game.class);
        when(game.getValidationStatus()).thenReturn(ValidationStatus.VALIDATED);
        when(game.getId()).thenReturn(1L);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(marketingActivityRepository.save(any(MarketingActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketingActivityResponse response = service.create(1L, createRequest());

        verify(game).moveToStage(GameStage.MARKETING);
        verify(marketingActivityRepository).save(any(MarketingActivity.class));
        assertEquals("YouTube", response.channel());
    }

    @Test
    void completingMarketingActivityStoresResultNotes() {
        Game game = mock(Game.class);
        when(game.getId()).thenReturn(1L);
        MarketingActivity activity = new MarketingActivity(
                game,
                MarketingActivityType.TRAILER,
                "YouTube",
                "Gameplay trailer",
                LocalDateTime.of(2026, 10, 1, 10, 0)
        );
        when(marketingActivityRepository.findById(1L)).thenReturn(Optional.of(activity));

        MarketingActivityResponse response = service.complete(
                1L,
                new CompleteMarketingActivityRequest("Trailer reached 500 views in the first day.")
        );

        assertNotNull(response.completedAt());
        assertEquals("Trailer reached 500 views in the first day.", response.resultNotes());
    }

    private CreateMarketingActivityRequest createRequest() {
        return new CreateMarketingActivityRequest(
                MarketingActivityType.TRAILER,
                "YouTube",
                "Gameplay trailer",
                LocalDateTime.of(2026, 10, 1, 10, 0)
        );
    }
}

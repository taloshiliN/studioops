package org.fromdesertdev.studioops.playtest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PlaytestController {
    private final PlaytestService playtestService;

    public PlaytestController(PlaytestService playtestService) {
        this.playtestService = playtestService;
    }

    @PostMapping("/games/{gameId}/playtests")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaytestResponse create(@PathVariable Long gameId,
                                   @Valid @RequestBody CreatePlaytestRequest request) {
        return playtestService.create(gameId, request);
    }

    @GetMapping("/games/{gameId}/playtests")
    public List<PlaytestResponse> findByGame(@PathVariable Long gameId) {
        return playtestService.findByGame(gameId);
    }

    @GetMapping("/playtests/{id}")
    public PlaytestResponse findById(@PathVariable Long id) {
        return playtestService.findById(id);
    }
}

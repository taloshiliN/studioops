package org.fromdesertdev.studioops.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/{gameId}/dashboard")
public class GameDashboardController {
    private final GameDashboardService gameDashboardService;

    public GameDashboardController(GameDashboardService gameDashboardService) {
        this.gameDashboardService = gameDashboardService;
    }

    @GetMapping
    public GameDashboardResponse getDashboard(@PathVariable Long gameId) {
        return gameDashboardService.getDashboard(gameId);
    }
}

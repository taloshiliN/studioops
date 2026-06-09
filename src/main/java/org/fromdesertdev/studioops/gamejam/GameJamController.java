package org.fromdesertdev.studioops.gamejam;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-jams")
public class GameJamController {
    private final GameJamService gameJamService;

    public GameJamController(GameJamService gameJamService){
        this.gameJamService = gameJamService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameJamResponse create(@Valid @RequestBody CreateGameJamRequest request){
        return gameJamService.create(request);
    }

    @GetMapping
    public List<GameJamResponse> findAll(){
        return gameJamService.findAll();
    }

    @GetMapping("/{id}")
    public GameJamResponse findById(@PathVariable Long id){
        return gameJamService.findById(id);
    }
}

package org.fromdesertdev.studioops.gamejam;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameJamService {
    private final GameJamRepository gameJamRepository;

    public GameJamService(GameJamRepository gameJamRepository){
        this.gameJamRepository = gameJamRepository;
    }

    @Transactional
    public GameJamResponse create(CreateGameJamRequest request){
        GameJam gameJam = new GameJam(
                request.name(),
                request.host(),
                request.theme(),
                request.startDate(),
                request.endDate(),
                request.url()
        );
        return GameJamResponse.from(gameJamRepository.save(gameJam));
    }

    @Transactional(readOnly = true)
    public List<GameJamResponse> findAll(){
        return gameJamRepository.findAll()
                .stream()
                .map(GameJamResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameJamResponse findById(long id){
        GameJam gameJam = gameJamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game jam not found"));
        return GameJamResponse.from(gameJam);
    }
}

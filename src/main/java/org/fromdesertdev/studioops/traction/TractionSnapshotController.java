package org.fromdesertdev.studioops.traction;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TractionSnapshotController {
    private final TractionSnapshotService tractionSnapshotService;

    public TractionSnapshotController(TractionSnapshotService tractionSnapshotService) {
        this.tractionSnapshotService = tractionSnapshotService;
    }

    @PostMapping("/traction-snapshots")
    @ResponseStatus(HttpStatus.CREATED)
    public TractionSnapshotResponse create(@Valid @RequestBody CreateTractionSnapshotRequest request) {
        return tractionSnapshotService.create(request);
    }

    @GetMapping("/games/{gameId}/traction")
    public List<TractionSnapshotResponse> findByGame(@PathVariable Long gameId) {
        return tractionSnapshotService.findByGame(gameId);
    }

    @GetMapping("/prototypes/{prototypeId}/traction")
    public List<TractionSnapshotResponse> findByPrototype(@PathVariable Long prototypeId) {
        return tractionSnapshotService.findByPrototype(prototypeId);
    }
}

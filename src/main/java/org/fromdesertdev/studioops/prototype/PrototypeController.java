package org.fromdesertdev.studioops.prototype;

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
public class PrototypeController {
    private final PrototypeService prototypeService;

    public PrototypeController(PrototypeService prototypeService) {
        this.prototypeService = prototypeService;
    }

    @PostMapping("/prototypes")
    @ResponseStatus(HttpStatus.CREATED)
    public PrototypeResponse create(@Valid @RequestBody CreatePrototypeRequest request) {
        return prototypeService.create(request);
    }

    @GetMapping("/prototypes/{id}")
    public PrototypeResponse findById(@PathVariable Long id) {
        return prototypeService.findById(id);
    }

    @GetMapping("/games/{gameId}/prototypes")
    public List<PrototypeResponse> findByGame(@PathVariable Long gameId) {
        return prototypeService.findByGame(gameId);
    }
}

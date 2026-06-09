package org.fromdesertdev.studioops.validation;

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
@RequestMapping("/api/games/{gameId}/validation-decisions")
public class ValidationDecisionController {
    private final ValidationDecisionService validationDecisionService;

    public ValidationDecisionController(ValidationDecisionService validationDecisionService) {
        this.validationDecisionService = validationDecisionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValidationDecisionResponse create(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateValidationDecisionRequest request
    ) {
        return validationDecisionService.create(gameId, request);
    }

    @GetMapping
    public List<ValidationDecisionResponse> findByGame(@PathVariable Long gameId) {
        return validationDecisionService.findByGame(gameId);
    }
}

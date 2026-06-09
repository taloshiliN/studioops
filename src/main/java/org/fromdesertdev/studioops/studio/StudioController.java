package org.fromdesertdev.studioops.studio;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studios")
public class StudioController {
    private final StudioService studioService;

    public StudioController(StudioService studioService) {
        this.studioService = studioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudioResponse create(
            @Valid @RequestBody CreateStudioRequest request,
            Authentication authentication
    ) {
        return studioService.create(request, authentication.getName());
    }

    @GetMapping
    public List<StudioResponse> findAll() {
        return studioService.findAll();
    }
}

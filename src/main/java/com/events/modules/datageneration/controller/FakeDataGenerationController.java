package com.events.modules.datageneration.controller;

import com.events.common.exception.BadRequestException;
import com.events.common.result.Result;
import com.events.modules.datageneration.service.IFakeDataGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Génération de fausses données : absente en production, et réservée aux admins ailleurs.
 */
@RestController
@Profile("!prod")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/data-generation")
@RequiredArgsConstructor
public class FakeDataGenerationController {

    private static final int MAX_EVENTS = 1000;

    private final IFakeDataGenerationService fakeDataGenerationService;

    @PostMapping
    public ResponseEntity<Result<Void>> generateFakeEvents(@RequestBody Integer numberOfEvents) {
        if (numberOfEvents == null || numberOfEvents < 1 || numberOfEvents > MAX_EVENTS) {
            throw new BadRequestException("numberOfEvents must be between 1 and " + MAX_EVENTS);
        }
       fakeDataGenerationService.generateEvents(numberOfEvents);

        return ResponseEntity.ok(Result.success());
    }
}

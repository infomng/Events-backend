package com.events.modules.datageneration.controller;

import com.events.common.result.Result;
import com.events.modules.datageneration.service.IFakeDataGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data-generation")
@RequiredArgsConstructor
public class FakeDataGenerationController {

    private final IFakeDataGenerationService fakeDataGenerationService;

    @PostMapping
    public ResponseEntity<Result<Void>> generateFakeEvents(@RequestBody Integer numberOfEvents) {
       fakeDataGenerationService.generateEvents(numberOfEvents);

        return ResponseEntity.ok(Result.success());
    }
}

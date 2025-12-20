package com.events.modules.event.controller;


import com.events.common.result.Result;
import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.EventDto;
import com.events.modules.event.dto.UpdateEventCommandDto;
import com.events.modules.event.service.IEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final IEventService service;

    @PostMapping()
    public ResponseEntity<Result<UUID>> create(@Valid @RequestBody CreateEventCommandDto command) {
        return ResponseEntity.ok(Result.success(service.createEvent(command)));
    }

    @GetMapping
    public List<EventDto> getAll() {
        return service.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventDto getById(@PathVariable Long id) {
        return service.getEventById(id);
    }

    @GetMapping("/nearby")
    public List<EventDto> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "5000") Double radius // 5 km par défaut
    ) {
        return service.getEventsNearby(lat, lon, radius);
    }

    @GetMapping("/{id}/update")
    public ResponseEntity<Result<Void>> updateEvent(@PathVariable Long id,@Valid @RequestBody UpdateEventCommandDto command) {
        service.updateEvent(id, command);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/incoming")
    public List<EventDto> getAllIncomingEvents() {
        return service.getAllIncomingEvents();
    }
}

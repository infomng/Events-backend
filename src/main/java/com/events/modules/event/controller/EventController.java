package com.events.modules.event.controller;


import com.events.common.result.Result;
import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.EventDto;
import com.events.modules.event.dto.GenerateSeatDto;
import com.events.modules.event.dto.UpdateEventCommandDto;
import com.events.modules.event.service.IEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Validated
public class EventController {

    private final IEventService eventService;

    @GetMapping("default-price-categories")
    public ResponseEntity<Result<List<String>>> getDefaultPriceCategories() {
        return ResponseEntity.ok(Result.success(eventService.getDefaultPriceCategories()));
    }

    @PostMapping()
    public ResponseEntity<Result<UUID>> create(@Valid @RequestBody CreateEventCommandDto command) {
        return ResponseEntity.ok(Result.success(eventService.createEvent(command)));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<Result<List<String>>> uploadImages(@PathVariable UUID id, @RequestBody MultipartFile[] files) throws IOException {
        return ResponseEntity.ok(Result.success(eventService.uploadEventImages(id, files)));
    }

    @PostMapping("/{id}/seats/generate")
    public ResponseEntity<Result<Void>> generateSeats(@PathVariable UUID id, @RequestParam List<GenerateSeatDto> generateSeatDto) {
        eventService.generateSeatsForEvent(id, generateSeatDto);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping
    public ResponseEntity<Result<Page<EventDto>>> getAll(
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "DESC", required = false) String sort,
                                 String country) {
        return ResponseEntity.ok(Result.success(eventService.getAllEvents(page, size, country)));
    }

    @GetMapping("/{id}")
    public EventDto getById(@PathVariable UUID id) {
        return eventService.getEventById(id);
    }

    @GetMapping("/nearby")
    public List<EventDto> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "5000") Double radius // 5 km par défaut
    ) {
        return eventService.getEventsNearby(lat, lon, radius);
    }

    @GetMapping("/{id}/update")
    public ResponseEntity<Result<Void>> updateEvent(@PathVariable UUID id,@Valid @RequestBody UpdateEventCommandDto command) {
        eventService.updateEvent(id, command);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/incoming")
    public List<EventDto> getAllIncomingEvents() {
        return eventService.getAllIncomingEvents();
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Result<Void>> softDeleteEvent(@PathVariable UUID eventId) {
        eventService.softDeleteEvent(eventId);
        return ResponseEntity.ok(Result.success());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Result<Void>> cancelEvent(@PathVariable UUID id) {
        eventService.cancelEvent(id);
        return ResponseEntity.ok(Result.success());
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<Result<Void>> publishEvent(@PathVariable UUID id) {
        eventService.publishEvent(id);
        return ResponseEntity.ok(Result.success());
    }
}

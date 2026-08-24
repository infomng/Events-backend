package com.events.modules.favorite.controller;

import com.events.common.result.Result;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.favorite.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Validated
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping("/events/{eventId}")
    public ResponseEntity<Result<Void>> addToFavorites(@PathVariable UUID eventId) {
        favoriteService.addFavorite(eventId);
        return ResponseEntity.ok(Result.success());
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Result<Void>> removeFromFavorites(@PathVariable UUID eventId) {
        favoriteService.removeFavorite(eventId);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/events")
    public ResponseEntity<Result<List<GetEventDto>>> getMyFavorites() {
        List<GetEventDto> favorites = favoriteService.getFavorites();
        return ResponseEntity.ok(Result.success(favorites));
    }
}

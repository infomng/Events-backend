package com.events.modules.favorite.service;

import com.events.modules.event.dto.GetEventDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IFavoriteService {
    /**
     * Add an event to current user's favorites
     * @param eventId the event ID to add
     */
    void addFavorite(UUID eventId);

    /**
     * Remove an event from current user's favorites
     * @param eventId the event ID to remove
     */
    void removeFavorite(UUID eventId);

    List<GetEventDto> getFavorites();
}

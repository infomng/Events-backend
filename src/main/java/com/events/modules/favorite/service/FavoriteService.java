package com.events.modules.favorite.service;

import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.exception.EventNotFoundException;
import com.events.modules.event.service.IEventService;
import com.events.modules.favorite.entity.Favorite;
import com.events.modules.favorite.exception.FavoriteAlreadyExistsException;
import com.events.modules.favorite.exception.FavoriteMaximumSizeReachedException;
import com.events.modules.favorite.exception.FavoriteNotFoundException;
import com.events.modules.favorite.repository.IFavoriteRepository;
import com.events.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService implements IFavoriteService {

    private final IAuthService authService;
    private final IFavoriteRepository favoriteRepository;
    private final IEventService eventService;


    @Override
    public void addFavorite(UUID eventId) {
        boolean eventExist = eventService.existById(eventId);
        if (!eventExist) {
            throw new EventNotFoundException(eventId);
        }

        var savedFavorite = favoriteRepository.findByEventIdAndUserId(eventId, authService.getCurrentUser().getId());
        if (savedFavorite.isPresent()) {
            throw new FavoriteAlreadyExistsException();
        }

        var count = favoriteRepository.countByUserId(authService.getCurrentUser().getId());

        if (count >= 30) {
            throw new FavoriteMaximumSizeReachedException();
        }

        User currentUser = authService.getCurrentUser();
        Favorite favorite = Favorite.create(eventId, currentUser.getId());
        favoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(UUID eventId) {
        var currentUser = authService.getCurrentUser();

        Favorite favorite = favoriteRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .orElseThrow(FavoriteNotFoundException::new);

        favoriteRepository.deleteById(favorite.getId());

    }

    @Override
    @Transactional(readOnly = true)
    public List<GetEventDto> getFavorites() {
        User currentUser = authService.getCurrentUser();
        List<UUID> favoritesIds = favoriteRepository.findEventIdByUserId(currentUser.getId());
        return eventService.getAllByIds(favoritesIds);
    }
}

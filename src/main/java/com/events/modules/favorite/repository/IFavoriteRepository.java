package com.events.modules.favorite.repository;

import com.events.modules.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFavoriteRepository extends JpaRepository<Favorite, UUID> {

    @Query("Select f From Favorite f where f.eventId = :eventId and f.userId = :userId")
    Optional<Favorite> findByEventIdAndUserId(UUID eventId, UUID userId);

    @Query("Select e.eventId From Favorite e where e.userId = :id")
    List<UUID> findEventIdByUserId(UUID id);

    Long countByUserId(UUID userId);
}

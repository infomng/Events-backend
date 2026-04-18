package com.events.modules.user.repository;

import com.events.modules.event.entity.Event;
import com.events.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = ?1")
    boolean existByEmail(String email);

    @Query("SELECT e FROM User u JOIN u.favoriteEvents e WHERE u.id = :userId AND e.isActive = true")
    Page<Event> findFavoriteEventsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM User u JOIN u.favoriteEvents e WHERE u.id = :userId AND e.id = :eventId AND e.isActive = true")
    boolean isEventFavorited(@Param("userId") UUID userId, @Param("eventId") UUID eventId);
}

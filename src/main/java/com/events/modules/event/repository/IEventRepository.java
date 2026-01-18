package com.events.modules.event.repository;

import com.events.modules.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IEventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE " +
            "FUNCTION('earth_distance', ll_to_earth(e.latitude, e.longitude), ll_to_earth(?1, ?2)) <= ?3")
    List<Event> findByLocationNear(Double latitude, Double longitude, Double radiusInMeters);

    @Query("SELECT e FROM Event e WHERE e.endDate > CURRENT_TIMESTAMP " +
            "AND e.isPublic = true " +
            "AND e.status = com.events.modules.event.enumeration.EventStatusEnum.PUBLISHED")
    List<Event> getAllIncomingEvents();


    @Query("SELECT e FROM Event e WHERE e.id = ?1 AND e.organizer.id = ?2")
    Optional<Event> findByIdAndOrganizerId(UUID eventId, UUID organizerId);
}

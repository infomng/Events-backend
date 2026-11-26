package com.events.modules.event.repository;

import com.events.modules.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IEventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {
    @Query("SELECT e FROM Event e WHERE " +
            "FUNCTION('earth_distance', ll_to_earth(e.latitude, e.longitude), ll_to_earth(?1, ?2)) <= ?3")
    List<Event> findByLocationNear(Double latitude, Double longitude, Double radiusInMeters);

    @Query("SELECT e FROM Event e WHERE e.endDate > CURRENT_TIMESTAMP " +
            "AND e.isPublic = true " +
            "AND e.status = com.events.modules.event.enumeration.EventStatusEnum.PUBLISHED")
    List<Event> getAllIncomingEvents();


    @Query("SELECT e FROM Event e WHERE e.id = ?1 AND e.organizer.id = ?2")
    Optional<Event> findByIdAndOrganizerId(UUID eventId, UUID organizerId);

    /**
     * Réserve des billets de façon atomique : la mise à jour n'a lieu que s'il reste assez de places.
     * @return 1 si la réservation a réussi, 0 sinon
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE PriceCategory pc SET pc.availableTickets = pc.availableTickets - :quantity, " +
            "pc.soldTickets = pc.soldTickets + :quantity " +
            "WHERE pc.id = :priceCategoryId AND pc.event.id = :eventId AND pc.availableTickets >= :quantity")
    int reservePriceCategoryTickets(@Param("eventId") UUID eventId,
                                    @Param("priceCategoryId") UUID priceCategoryId,
                                    @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE PriceCategory pc SET pc.availableTickets = pc.availableTickets + :quantity, " +
            "pc.soldTickets = pc.soldTickets - :quantity " +
            "WHERE pc.id = :priceCategoryId AND pc.event.id = :eventId AND pc.soldTickets >= :quantity")
    int releasePriceCategoryTickets(@Param("eventId") UUID eventId,
                                    @Param("priceCategoryId") UUID priceCategoryId,
                                    @Param("quantity") int quantity);

    /** Ajuste le compteur agrégé de l'événement (delta négatif pour réserver, positif pour libérer). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets + :delta WHERE e.id = :eventId")
    int adjustEventAvailableTickets(@Param("eventId") UUID eventId, @Param("delta") int delta);
}

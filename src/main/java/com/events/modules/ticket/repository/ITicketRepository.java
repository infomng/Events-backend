package com.events.modules.ticket.repository;

import com.events.modules.ticket.entity.Ticket;
import com.events.modules.ticket.enumeration.TicketStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByUserId(UUID userId);

    List<Ticket> findByBookingId(UUID bookingId);

    boolean existsByBookingId(UUID bookingId);

    Optional<Ticket> findByTicketCode(String ticketCode);

    long countByEventIdAndStatus(UUID eventId, TicketStatusEnum status);

    /**
     * Passe un billet valide en "utilisé" de façon atomique : deux scans simultanés du même billet
     * ne peuvent pas réussir tous les deux.
     * @return 1 si le billet a été validé, 0 s'il n'était plus valide
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Ticket t SET t.status = :used, t.checkedInAt = :now, t.checkedInBy = :staffId " +
            "WHERE t.ticketCode = :ticketCode AND t.eventId = :eventId AND t.status = :valid")
    int markAsUsed(@Param("ticketCode") String ticketCode,
                   @Param("eventId") UUID eventId,
                   @Param("staffId") UUID staffId,
                   @Param("now") LocalDateTime now,
                   @Param("valid") TicketStatusEnum valid,
                   @Param("used") TicketStatusEnum used);
}

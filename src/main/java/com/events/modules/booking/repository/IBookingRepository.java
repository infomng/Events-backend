package com.events.modules.booking.repository;

import com.events.modules.booking.entity.Booking;
import com.events.modules.event.enumeration.BookingStatusEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IBookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(UUID id, UUID userId);

    /** Verrou pessimiste : expiration, annulation et paiement ne doivent pas modifier la même réservation en parallèle. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findForUpdateById(@Param("id") UUID id);

    @Query("SELECT b.id FROM Booking b WHERE b.status = :status AND b.expiresAt < :now")
    List<UUID> findIdsByStatusAndExpiresAtBefore(@Param("status") BookingStatusEnum status,
                                                @Param("now") LocalDateTime now);
}

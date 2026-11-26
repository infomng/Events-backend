package com.events.modules.booking.scheduler;

import com.events.modules.booking.service.IBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Libère régulièrement les places des réservations non payées dans les temps.
 * Chaque réservation est traitée dans sa propre transaction : un échec n'empêche pas les autres.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationJob {

    private final IBookingService bookingService;

    @Scheduled(fixedDelayString = "${booking.expiration-check-ms:60000}")
    public void expireOverdueBookings() {
        List<UUID> overdue = bookingService.findOverdueBookingIds();
        for (UUID bookingId : overdue) {
            try {
                bookingService.expireBooking(bookingId);
            } catch (RuntimeException e) {
                log.error("Failed to expire booking {}: {}", bookingId, e.getMessage(), e);
            }
        }
    }
}

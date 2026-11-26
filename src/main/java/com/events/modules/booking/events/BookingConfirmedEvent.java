package com.events.modules.booking.events;

import com.events.modules.ticket.dto.TicketDto;

import java.util.List;
import java.util.UUID;

/**
 * Publié quand une réservation est payée et ses billets émis.
 */
public record BookingConfirmedEvent(
        UUID bookingId,
        String bookingReference,
        String eventName,
        String userEmail,
        String userFullName,
        List<TicketDto> tickets
) {
}

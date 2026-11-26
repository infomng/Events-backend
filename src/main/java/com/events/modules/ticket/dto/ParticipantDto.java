package com.events.modules.ticket.dto;

import com.events.modules.ticket.enumeration.TicketStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Un billet émis pour un événement, vu par l'organisateur.
 */
public record ParticipantDto(
        UUID ticketId,
        String ticketCode,
        String category,
        String holderName,
        String holderEmail,
        TicketStatusEnum status,
        LocalDateTime checkedInAt,
        UUID bookingId
) {
}

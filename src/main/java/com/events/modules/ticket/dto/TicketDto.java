package com.events.modules.ticket.dto;

import com.events.modules.ticket.enumeration.TicketStatusEnum;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TicketDto(
        UUID id,
        UUID eventId,
        String eventName,
        UUID userId,
        String userName,
        String ticketCode,
        Double price,
        String seatNumber,
        TicketStatusEnum status,
        byte[] qrCode
) {
}

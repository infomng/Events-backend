package com.events.modules.ticket.dto;

import java.util.UUID;

public record CheckInStatsDto(
        UUID eventId,
        long issuedTickets,
        long checkedIn,
        long remaining,
        long cancelledTickets
) {
}

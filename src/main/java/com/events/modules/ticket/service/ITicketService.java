package com.events.modules.ticket.service;

import com.events.modules.ticket.dto.TicketDto;
import com.events.modules.ticket.dto.TicketLineDto;

import java.util.List;
import java.util.UUID;

public interface ITicketService {

    /**
     * Émet les billets d'une réservation payée. Idempotent : si les billets existent déjà,
     * ils sont retournés sans en créer de nouveaux.
     */
    List<TicketDto> generateTicketsForBooking(UUID bookingId, UUID userId, UUID eventId, List<TicketLineDto> lines);

    /**
     * Annule les billets encore valides d'une réservation (remboursement, annulation).
     */
    void cancelTicketsForBooking(UUID bookingId);

    TicketDto getTicketById(UUID ticketId);

    List<TicketDto> getTicketsForUser(UUID userId);
}

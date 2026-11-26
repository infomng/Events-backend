package com.events.modules.ticket.service;

import com.events.modules.ticket.dto.CheckInRequestDto;
import com.events.modules.ticket.dto.CheckInResultDto;
import com.events.modules.ticket.dto.CheckInStatsDto;
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

    /**
     * Billet de l'utilisateur courant (ou d'un événement qu'il contrôle).
     */
    TicketDto getTicketById(UUID ticketId);

    /**
     * Scanne un QR code à l'entrée d'un événement. Réservé à l'organisateur, au staff et aux admins.
     * Un billet ne peut être validé qu'une seule fois, même en cas de scans simultanés.
     */
    CheckInResultDto checkIn(CheckInRequestDto dto);

    /** Statistiques d'entrée d'un événement (réservé aux mêmes personnes que le scan). */
    CheckInStatsDto getCheckInStats(UUID eventId);

    List<TicketDto> getTicketsForUser(UUID userId);
}

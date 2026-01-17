package com.events.modules.ticket.service;

import com.events.modules.booking.entity.Booking;
import com.events.modules.ticket.dto.TicketDto;

import java.util.List;
import java.util.UUID;

public interface ITicketService {
    List<TicketDto> generateTicketsForBooking(Booking booking);
    TicketDto getTicketById(UUID ticketId);
    List<TicketDto> getTicketsForUser(UUID userId);
}

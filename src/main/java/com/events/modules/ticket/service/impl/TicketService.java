package com.events.modules.ticket.service.impl;

import com.events.modules.booking.entity.Booking;
import com.events.modules.event.entity.aggregate.BookingSeat;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.service.IEventService;
import com.events.modules.qrcode.service.IQrCodeService;
import com.events.modules.ticket.dto.TicketDto;
import com.events.modules.ticket.entity.Ticket;
import com.events.modules.ticket.enumeration.TicketStatusEnum;
import com.events.modules.ticket.repository.ITicketRepository;
import com.events.modules.ticket.service.ITicketService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketService implements ITicketService {

    private final ITicketRepository ticketRepository;
    private final IUserService userService;
    private final IEventService eventService;
    private final IQrCodeService qrCodeService;

    @Override
    public List<TicketDto> generateTicketsForBooking(Booking booking) {
        User user = userService.findById(booking.getUserId());
        GetEventDto event = eventService.getEventById(booking.getEventId());

        return booking.getBookingSeats().stream()
                .map(bookingSeat -> createTicket(event, user, bookingSeat))
                .map(this::toDto)
                .toList();
    }

    @Override
    public TicketDto getTicketById(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Ticket not found")); // Replace with specific exception
    }

    @Override
    public List<TicketDto> getTicketsForUser(UUID userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private Ticket createTicket(GetEventDto event, User user, BookingSeat bookingSeat) {
        String ticketCode = UUID.randomUUID().toString();
        String qrContent = "Ticket Code: " + ticketCode + "\nEvent: " + event.name() + "\nUser: " + user.getFullName();
        byte[] qrCode = qrCodeService.generateQrCode(qrContent, 250, 250);

        Ticket ticket = Ticket.builder()
                .eventId(event.id())
                .user(user)
                .ticketCode(ticketCode)
                .price(bookingSeat.getPrice().doubleValue())
                .seatNumber(bookingSeat.getSeat().getSeatNumber())
                .status(TicketStatusEnum.VALID)
                .qrCode(qrCode)
                .build();

        return ticketRepository.save(ticket);
    }

    private TicketDto toDto(Ticket ticket) {
        return TicketDto.builder()
                .id(ticket.getId())
                .eventId(ticket.getEventId())
                .eventName(eventService.getEventById(ticket.getEventId()).name())
                .userId(ticket.getUser().getId())
                .userName(ticket.getUser().getFullName())
                .ticketCode(ticket.getTicketCode())
                .price(ticket.getPrice())
                .seatNumber(ticket.getSeatNumber())
                .status(ticket.getStatus())
                .qrCode(ticket.getQrCode())
                .build();
    }
}


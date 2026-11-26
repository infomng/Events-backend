package com.events.modules.ticket.service.impl;

import com.events.common.exception.ResourceNotFoundException;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.service.IEventService;
import com.events.modules.qrcode.service.IQrCodeService;
import com.events.modules.ticket.dto.TicketDto;
import com.events.modules.ticket.dto.TicketLineDto;
import com.events.modules.ticket.entity.Ticket;
import com.events.modules.ticket.enumeration.TicketStatusEnum;
import com.events.modules.ticket.repository.ITicketRepository;
import com.events.modules.ticket.service.ITicketService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TicketService implements ITicketService {

    private static final int QR_CODE_SIZE = 250;

    private final ITicketRepository ticketRepository;
    private final IUserService userService;
    private final IEventService eventService;
    private final IQrCodeService qrCodeService;

    @Override
    public List<TicketDto> generateTicketsForBooking(UUID bookingId, UUID userId, UUID eventId, List<TicketLineDto> lines) {
        GetEventDto event = eventService.getEventById(eventId);

        // Idempotence : une confirmation rejouée (webhook + confirmation client) ne double pas les billets.
        if (ticketRepository.existsByBookingId(bookingId)) {
            return ticketRepository.findByBookingId(bookingId).stream()
                    .map(ticket -> toDto(ticket, event.name()))
                    .toList();
        }

        User user = userService.findById(userId);
        List<Ticket> tickets = new ArrayList<>();
        for (TicketLineDto line : lines) {
            for (int i = 0; i < line.quantity(); i++) {
                tickets.add(buildTicket(bookingId, eventId, user, line));
            }
        }

        List<Ticket> saved = ticketRepository.saveAll(tickets);
        log.info("Issued {} ticket(s) for booking {}", saved.size(), bookingId);

        return saved.stream().map(ticket -> toDto(ticket, event.name())).toList();
    }

    @Override
    public void cancelTicketsForBooking(UUID bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatusEnum.VALID)
                .forEach(ticket -> ticket.setStatus(TicketStatusEnum.CANCELLED));
        ticketRepository.saveAll(tickets);
        log.info("Cancelled tickets of booking {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketById(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsForUser(UUID userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    private Ticket buildTicket(UUID bookingId, UUID eventId, User user, TicketLineDto line) {
        String ticketCode = UUID.randomUUID().toString();
        // Le QR code ne contient que le code du billet : aucune donnée personnelle.
        byte[] qrCode = qrCodeService.generateQrCode(ticketCode, QR_CODE_SIZE, QR_CODE_SIZE);

        return Ticket.builder()
                .eventId(eventId)
                .bookingId(bookingId)
                .category(line.category())
                .user(user)
                .ticketCode(ticketCode)
                .price(line.unitPrice().doubleValue())
                .status(TicketStatusEnum.VALID)
                .qrCode(qrCode)
                .build();
    }

    private TicketDto toDto(Ticket ticket) {
        return toDto(ticket, eventService.getEventById(ticket.getEventId()).name());
    }

    private TicketDto toDto(Ticket ticket, String eventName) {
        return TicketDto.builder()
                .id(ticket.getId())
                .eventId(ticket.getEventId())
                .eventName(eventName)
                .bookingId(ticket.getBookingId())
                .category(ticket.getCategory())
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

package com.events.modules.ticket.service.impl;

import com.events.common.exception.ResourceNotFoundException;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.event.exception.EventForbidenException;
import com.events.modules.event.service.IEventService;
import com.events.modules.qrcode.service.IQrCodeService;
import com.events.modules.ticket.dto.CheckInRequestDto;
import com.events.modules.ticket.dto.CheckInResultDto;
import com.events.modules.ticket.dto.CheckInStatsDto;
import com.events.modules.ticket.dto.TicketDto;
import com.events.modules.ticket.dto.TicketLineDto;
import com.events.modules.ticket.entity.Ticket;
import com.events.modules.ticket.enumeration.CheckInResultEnum;
import com.events.modules.ticket.enumeration.TicketStatusEnum;
import com.events.modules.ticket.repository.ITicketRepository;
import com.events.modules.ticket.security.TicketTokenSigner;
import com.events.modules.ticket.service.ITicketService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final IAuthService authService;
    private final TicketTokenSigner ticketTokenSigner;
    private final Clock clock;

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
        User currentUser = authService.getCurrentUser();
        return ticketRepository.findById(ticketId)
                // Même réponse si absent ou non autorisé : on ne révèle pas l'existence du billet
                .filter(ticket -> ticket.getUser().getId().equals(currentUser.getId())
                        || eventService.isCurrentUserEntryManager(ticket.getEventId()))
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    @Override
    public CheckInResultDto checkIn(CheckInRequestDto dto) {
        assertEntryManager(dto.eventId());
        User staff = authService.getCurrentUser();

        Optional<Ticket> scanned = ticketTokenSigner.verify(dto.qrPayload())
                .flatMap(ticketRepository::findByTicketCode);
        if (scanned.isEmpty()) {
            log.warn("Rejected invalid QR code at event {} (scanned by {})", dto.eventId(), staff.getId());
            return CheckInResultDto.invalid();
        }

        Ticket ticket = scanned.get();
        if (!ticket.getEventId().equals(dto.eventId())) {
            return result(CheckInResultEnum.WRONG_EVENT, false, ticket);
        }

        int updated = ticketRepository.markAsUsed(ticket.getTicketCode(), dto.eventId(), staff.getId(),
                LocalDateTime.now(clock), TicketStatusEnum.VALID, TicketStatusEnum.USED);
        if (updated == 1) {
            Ticket used = ticketRepository.findByTicketCode(ticket.getTicketCode()).orElse(ticket);
            log.info("Ticket {} checked in at event {} by {}", used.getTicketCode(), dto.eventId(), staff.getId());
            return result(CheckInResultEnum.ACCEPTED, true, used);
        }

        // Le billet n'était plus valide : on relit son état réel pour expliquer le refus
        Ticket current = ticketRepository.findByTicketCode(ticket.getTicketCode()).orElse(ticket);
        CheckInResultEnum reason = current.getStatus() == TicketStatusEnum.CANCELLED
                ? CheckInResultEnum.CANCELLED
                : CheckInResultEnum.ALREADY_USED;
        return result(reason, false, current);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInStatsDto getCheckInStats(UUID eventId) {
        assertEntryManager(eventId);
        long valid = ticketRepository.countByEventIdAndStatus(eventId, TicketStatusEnum.VALID);
        long used = ticketRepository.countByEventIdAndStatus(eventId, TicketStatusEnum.USED);
        long cancelled = ticketRepository.countByEventIdAndStatus(eventId, TicketStatusEnum.CANCELLED);
        return new CheckInStatsDto(eventId, valid + used, used, valid, cancelled);
    }

    private void assertEntryManager(UUID eventId) {
        if (!eventService.isCurrentUserEntryManager(eventId)) {
            throw new EventForbidenException(eventId);
        }
    }

    private static CheckInResultDto result(CheckInResultEnum result, boolean allowed, Ticket ticket) {
        return new CheckInResultDto(
                result,
                allowed,
                ticket.getTicketCode(),
                ticket.getCategory(),
                ticket.getUser() != null ? ticket.getUser().getFullName() : null,
                ticket.getCheckedInAt()
        );
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
        // QR code signé : infalsifiable, et sans aucune donnée personnelle.
        byte[] qrCode = qrCodeService.generateQrCode(ticketTokenSigner.sign(ticketCode), QR_CODE_SIZE, QR_CODE_SIZE);

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

package com.events.modules.booking.service;

import com.events.common.exception.BadRequestException;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.booking.dto.BookingPaymentDto;
import com.events.modules.booking.dto.CheckoutRequestDto;
import com.events.modules.booking.dto.GetBookingDto;
import com.events.modules.booking.dto.mapper.IBookingMapper;
import com.events.modules.booking.entity.Booking;
import com.events.modules.booking.entity.BookingItem;
import com.events.modules.booking.events.BookingConfirmedEvent;
import com.events.modules.booking.events.BookingReleasedEvent;
import com.events.modules.booking.exception.BookingNotFoundException;
import com.events.modules.booking.repository.IBookingRepository;
import com.events.modules.cart.dto.GetCartItemDto;
import com.events.modules.cart.service.ICartService;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.dto.GetPriceCategoryDto;
import com.events.modules.event.enumeration.BookingStatusEnum;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.exception.PriceCategoryNotFoundException;
import com.events.modules.event.exception.TicketsSoldOutException;
import com.events.modules.event.service.IEventService;
import com.events.modules.ticket.dto.TicketDto;
import com.events.modules.ticket.dto.TicketLineDto;
import com.events.modules.ticket.service.ITicketService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingService implements IBookingService {

    private final IBookingRepository bookingRepository;
    private final IBookingMapper bookingMapper;
    private final IAuthService authService;
    private final ICartService cartService;
    private final IEventService eventService;
    private final ITicketService ticketService;
    private final IUserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    /** Durée pendant laquelle les places sont bloquées en attendant le paiement. */
    @Value("${booking.hold-minutes:15}")
    private long holdMinutes = 15;

    @Override
    public GetBookingDto checkout(CheckoutRequestDto dto) {
        User currentUser = authService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now(clock);

        GetEventDto event = eventService.getEventById(dto.eventId());
        assertTicketsOnSale(event, now);

        List<GetCartItemDto> cartItems = cartService.takeItemsForEvent(currentUser.getId(), event.id());

        Booking booking = Booking.builder()
                .eventId(event.id())
                .eventName(event.name())
                .userId(currentUser.getId())
                .status(BookingStatusEnum.PENDING)
                .expiresAt(now.plusMinutes(holdMinutes))
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (GetCartItemDto cartItem : cartItems) {
            // Prix courant de l'événement, pas celui figé dans le panier
            GetPriceCategoryDto priceCategory = event.priceCategories().stream()
                    .filter(pc -> pc.id().equals(cartItem.priceCategoryId()))
                    .findFirst()
                    .orElseThrow(() -> new PriceCategoryNotFoundException(event.id()));

            // Blocage atomique des places : lève TicketsSoldOutException et annule toute la commande si épuisé
            eventService.reserveTickets(event.id(), priceCategory.id(), cartItem.quantity());

            BigDecimal lineTotal = priceCategory.price().multiply(BigDecimal.valueOf(cartItem.quantity()));
            booking.addItem(BookingItem.builder()
                    .priceCategoryId(priceCategory.id())
                    .priceCategoryName(priceCategory.name())
                    .quantity(cartItem.quantity())
                    .unitPrice(priceCategory.price())
                    .totalPrice(lineTotal)
                    .build());
            total = total.add(lineTotal);
        }
        booking.setTotalAmount(total);
        booking = bookingRepository.save(booking);

        log.info("Booking {} created for user {} on event {} (total {})",
                booking.getBookingReference(), currentUser.getId(), event.id(), total);

        // Billets gratuits : pas de paiement, confirmation immédiate
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            confirm(booking, null);
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetBookingDto> getMyBookings(int page, int size) {
        User currentUser = authService.getCurrentUser();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingRepository.findByUserId(currentUser.getId(), pageable).map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public GetBookingDto getMyBooking(UUID bookingId) {
        User currentUser = authService.getCurrentUser();
        return bookingRepository.findByIdAndUserId(bookingId, currentUser.getId())
                .map(bookingMapper::toDto)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Override
    public void cancelMyBooking(UUID bookingId) {
        User currentUser = authService.getCurrentUser();
        Booking booking = bookingRepository.findForUpdateById(bookingId)
                .filter(b -> b.getUserId().equals(currentUser.getId()))
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatusEnum.PENDING) {
            throw new BadRequestException("Only a booking awaiting payment can be cancelled: " + bookingId);
        }

        release(booking, BookingStatusEnum.CANCELLED);
        log.info("Booking {} cancelled by user {}", booking.getBookingReference(), currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingPaymentDto getPayableBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUserId().equals(userId))
                // Même erreur si absente ou appartenant à un autre utilisateur : pas de fuite d'information
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.isAwaitingPayment(LocalDateTime.now(clock))) {
            throw new BadRequestException("Booking is not awaiting payment: " + bookingId);
        }

        if (booking.getTotalAmount() == null || booking.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Booking has no payable amount: " + bookingId);
        }

        return new BookingPaymentDto(
                booking.getId(),
                booking.getUserId(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getBookingReference()
        );
    }

    @Override
    public boolean confirmBookingPayment(UUID bookingId, String paymentId) {
        Booking booking = bookingRepository.findForUpdateById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() == BookingStatusEnum.CONFIRMED) {
            return true; // déjà confirmée (rejeu)
        }

        if (booking.getStatus() != BookingStatusEnum.PENDING) {
            // Paiement arrivé après expiration ou annulation : on tente de reprendre les places
            if (!reacquireTickets(booking)) {
                log.warn("Booking {} paid after release and tickets are no longer available",
                        booking.getBookingReference());
                return false;
            }
        }

        confirm(booking, paymentId);
        return true;
    }

    @Override
    public void cancelBookingAfterRefund(UUID bookingId) {
        Booking booking = bookingRepository.findForUpdateById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        ticketService.cancelTicketsForBooking(bookingId);
        if (booking.getStatus() == BookingStatusEnum.CONFIRMED || booking.getStatus() == BookingStatusEnum.PENDING) {
            releaseTickets(booking);
        }
        booking.setStatus(BookingStatusEnum.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findOverdueBookingIds() {
        return bookingRepository.findIdsByStatusAndExpiresAtBefore(BookingStatusEnum.PENDING, LocalDateTime.now(clock));
    }

    @Override
    public void expireBooking(UUID bookingId) {
        bookingRepository.findForUpdateById(bookingId)
                .filter(b -> b.getStatus() == BookingStatusEnum.PENDING)
                .filter(b -> !b.isAwaitingPayment(LocalDateTime.now(clock)))
                .ifPresent(booking -> {
                    release(booking, BookingStatusEnum.EXPIRED);
                    log.info("Booking {} expired, tickets released", booking.getBookingReference());
                });
    }

    private void confirm(Booking booking, String paymentId) {
        booking.confirm(paymentId);
        bookingRepository.save(booking);

        List<TicketLineDto> lines = booking.getItems().stream()
                .map(item -> new TicketLineDto(item.getPriceCategoryName(), item.getUnitPrice(), item.getQuantity()))
                .toList();
        List<TicketDto> tickets = ticketService.generateTicketsForBooking(
                booking.getId(), booking.getUserId(), booking.getEventId(), lines);

        User user = userService.findById(booking.getUserId());
        eventPublisher.publishEvent(new BookingConfirmedEvent(
                booking.getId(),
                booking.getBookingReference(),
                booking.getEventName(),
                user.getEmail(),
                user.getFullName(),
                tickets
        ));
        log.info("Booking {} confirmed, {} ticket(s) issued", booking.getBookingReference(), tickets.size());
    }

    private void release(Booking booking, BookingStatusEnum finalStatus) {
        releaseTickets(booking);
        booking.setStatus(finalStatus);
        bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingReleasedEvent(booking.getId()));
    }

    private void releaseTickets(Booking booking) {
        booking.getItems().forEach(item ->
                eventService.releaseTickets(booking.getEventId(), item.getPriceCategoryId(), item.getQuantity()));
    }

    /**
     * Reprend les places d'une réservation libérée. Tout ou rien : si une catégorie est épuisée,
     * les places déjà reprises sont rendues.
     */
    private boolean reacquireTickets(Booking booking) {
        List<BookingItem> reserved = new ArrayList<>();
        try {
            for (BookingItem item : booking.getItems()) {
                eventService.reserveTickets(booking.getEventId(), item.getPriceCategoryId(), item.getQuantity());
                reserved.add(item);
            }
            return true;
        } catch (TicketsSoldOutException e) {
            reserved.forEach(item ->
                    eventService.releaseTickets(booking.getEventId(), item.getPriceCategoryId(), item.getQuantity()));
            return false;
        }
    }

    private static void assertTicketsOnSale(GetEventDto event, LocalDateTime now) {
        if (event.status() != EventStatusEnum.PUBLISHED) {
            throw new BadRequestException("Event is not open for booking: " + event.id());
        }
        if (event.startDate() != null && !event.startDate().isAfter(now)) {
            throw new BadRequestException("Event has already started: " + event.id());
        }
        if (event.ticketSalesStartDate() != null && now.isBefore(event.ticketSalesStartDate())) {
            throw new BadRequestException("Ticket sales have not started yet for event " + event.id());
        }
        if (event.ticketSalesEndDate() != null && now.isAfter(event.ticketSalesEndDate())) {
            throw new BadRequestException("Ticket sales are closed for event " + event.id());
        }
    }
}

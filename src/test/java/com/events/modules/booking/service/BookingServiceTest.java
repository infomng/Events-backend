package com.events.modules.booking.service;

import com.events.common.exception.BadRequestException;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.booking.dto.CheckoutRequestDto;
import com.events.modules.booking.dto.SalesReportDto;
import com.events.modules.booking.dto.SalesRowDto;
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
import com.events.modules.event.exception.TicketsSoldOutException;
import com.events.modules.event.service.IEventService;
import com.events.modules.ticket.dto.CheckInStatsDto;
import com.events.modules.ticket.service.ITicketService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 12, 0);

    @Mock private IBookingRepository bookingRepository;
    @Mock private IBookingMapper bookingMapper;
    @Mock private IAuthService authService;
    @Mock private ICartService cartService;
    @Mock private IEventService eventService;
    @Mock private ITicketService ticketService;
    @Mock private IUserService userService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private BookingService bookingService;
    private User currentUser;
    private UUID eventId;
    private UUID regularId;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        bookingService = new BookingService(bookingRepository, bookingMapper, authService, cartService,
                eventService, ticketService, userService, eventPublisher, clock);

        currentUser = User.builder().id(UUID.randomUUID()).email("buyer@example.com").fullName("Buyer").build();
        eventId = UUID.randomUUID();
        regularId = UUID.randomUUID();

        given(authService.getCurrentUser()).willReturn(currentUser);
        given(userService.findById(currentUser.getId())).willReturn(currentUser);
        given(bookingRepository.save(any(Booking.class))).willAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) {
                b.setId(UUID.randomUUID());
            }
            return b;
        });
        given(ticketService.generateTicketsForBooking(any(), any(), any(), any())).willReturn(List.of());
    }

    private GetEventDto event(EventStatusEnum status, BigDecimal price) {
        return GetEventDto.builder()
                .id(eventId)
                .name("Concert")
                .status(status)
                .startDate(NOW.plusDays(10))
                .endDate(NOW.plusDays(10).plusHours(3))
                .ticketSalesStartDate(NOW.minusDays(1))
                .ticketSalesEndDate(NOW.plusDays(9))
                .priceCategories(List.of(GetPriceCategoryDto.builder()
                        .id(regularId).name("REGULAR").price(price).totalTickets(100).availableTickets(50).build()))
                .build();
    }

    private GetCartItemDto cartItem(int quantity, BigDecimal snapshotPrice) {
        return new GetCartItemDto(UUID.randomUUID(), eventId, "Concert", regularId, "REGULAR",
                quantity, snapshotPrice, snapshotPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    private Booking pendingBooking(LocalDateTime expiresAt, BookingStatusEnum status) {
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .eventName("Concert")
                .userId(currentUser.getId())
                .status(status)
                .expiresAt(expiresAt)
                .totalAmount(new BigDecimal("40.00"))
                .bookingReference("BK-TEST")
                .build();
        booking.addItem(BookingItem.builder()
                .priceCategoryId(regularId).priceCategoryName("REGULAR")
                .quantity(2).unitPrice(new BigDecimal("20.00")).totalPrice(new BigDecimal("40.00"))
                .build());
        return booking;
    }

    @Nested
    class CheckoutBookingTest {

        @Test
        void holdsTicketsAtCurrentPriceAndSetsPaymentDeadline() {
            given(eventService.getEventById(eventId)).willReturn(event(EventStatusEnum.PUBLISHED, new BigDecimal("25.00")));
            // Le panier a figé un ancien prix : c'est le prix courant qui doit être retenu
            given(cartService.takeItemsForEvent(currentUser.getId(), eventId))
                    .willReturn(List.of(cartItem(2, new BigDecimal("10.00"))));

            bookingService.checkout(new CheckoutRequestDto(eventId));

            verify(eventService).reserveTickets(eventId, regularId, 2);
            ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(saved.capture());
            Booking booking = saved.getValue();
            assertThat(booking.getTotalAmount()).isEqualByComparingTo("50.00");
            assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.PENDING);
            assertThat(booking.getExpiresAt()).isEqualTo(NOW.plusMinutes(15));
            verify(ticketService, never()).generateTicketsForBooking(any(), any(), any(), any());
        }

        @Test
        void rejectsEventThatIsNotPublished() {
            given(eventService.getEventById(eventId)).willReturn(event(EventStatusEnum.DRAFT, BigDecimal.TEN));

            assertThatThrownBy(() -> bookingService.checkout(new CheckoutRequestDto(eventId)))
                    .isInstanceOf(BadRequestException.class);

            verify(cartService, never()).takeItemsForEvent(any(), any());
            verify(eventService, never()).reserveTickets(any(), any(), anyInt());
        }

        @Test
        void soldOutCategoryAbortsCheckout() {
            given(eventService.getEventById(eventId)).willReturn(event(EventStatusEnum.PUBLISHED, BigDecimal.TEN));
            given(cartService.takeItemsForEvent(currentUser.getId(), eventId))
                    .willReturn(List.of(cartItem(3, BigDecimal.TEN)));
            willThrow(new TicketsSoldOutException(regularId, 3)).given(eventService).reserveTickets(eventId, regularId, 3);

            assertThatThrownBy(() -> bookingService.checkout(new CheckoutRequestDto(eventId)))
                    .isInstanceOf(TicketsSoldOutException.class);

            verify(bookingRepository, never()).save(any());
        }

        @Test
        void freeBookingIsConfirmedImmediately() {
            given(eventService.getEventById(eventId)).willReturn(event(EventStatusEnum.PUBLISHED, BigDecimal.ZERO));
            given(cartService.takeItemsForEvent(currentUser.getId(), eventId))
                    .willReturn(List.of(cartItem(1, BigDecimal.ZERO)));

            bookingService.checkout(new CheckoutRequestDto(eventId));

            verify(ticketService).generateTicketsForBooking(any(), any(), any(), any());
            verify(eventPublisher).publishEvent(any(BookingConfirmedEvent.class));
        }
    }

    @Nested
    class ConfirmPaymentBookingTest {

        @Test
        void pendingBookingIsConfirmedAndTicketsIssued() {
            Booking booking = pendingBooking(NOW.plusMinutes(5), BookingStatusEnum.PENDING);
            given(bookingRepository.findForUpdateById(booking.getId())).willReturn(Optional.of(booking));

            boolean honoured = bookingService.confirmBookingPayment(booking.getId(), "pay-1");

            assertThat(honoured).isTrue();
            assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.CONFIRMED);
            assertThat(booking.getExpiresAt()).isNull();
            verify(ticketService).generateTicketsForBooking(any(), any(), any(), any());
            verify(eventPublisher).publishEvent(any(BookingConfirmedEvent.class));
        }

        @Test
        void expiredBookingIsHonouredWhenTicketsAreStillAvailable() {
            Booking booking = pendingBooking(NOW.minusMinutes(5), BookingStatusEnum.EXPIRED);
            given(bookingRepository.findForUpdateById(booking.getId())).willReturn(Optional.of(booking));

            boolean honoured = bookingService.confirmBookingPayment(booking.getId(), "pay-1");

            assertThat(honoured).isTrue();
            verify(eventService).reserveTickets(eventId, regularId, 2);
            assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.CONFIRMED);
        }

        @Test
        void expiredBookingIsNotHonouredWhenSoldOut() {
            Booking booking = pendingBooking(NOW.minusMinutes(5), BookingStatusEnum.EXPIRED);
            given(bookingRepository.findForUpdateById(booking.getId())).willReturn(Optional.of(booking));
            willThrow(new TicketsSoldOutException(regularId, 2)).given(eventService).reserveTickets(eventId, regularId, 2);

            boolean honoured = bookingService.confirmBookingPayment(booking.getId(), "pay-1");

            assertThat(honoured).isFalse();
            assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.EXPIRED);
            verify(ticketService, never()).generateTicketsForBooking(any(), any(), any(), any());
        }

        @Test
        void payableBookingMustNotBeExpired() {
            Booking booking = pendingBooking(NOW.minusMinutes(1), BookingStatusEnum.PENDING);
            given(bookingRepository.findById(booking.getId())).willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.getPayableBooking(booking.getId(), currentUser.getId()))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    class ReleaseBookingTest {

        @Test
        void overdueBookingIsExpiredAndTicketsReleased() {
            Booking booking = pendingBooking(NOW.minusMinutes(1), BookingStatusEnum.PENDING);
            given(bookingRepository.findForUpdateById(booking.getId())).willReturn(Optional.of(booking));

            bookingService.expireBooking(booking.getId());

            assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.EXPIRED);
            verify(eventService).releaseTickets(eventId, regularId, 2);
            verify(eventPublisher).publishEvent(new BookingReleasedEvent(booking.getId()));
        }

        @Test
        void bookingStillWithinDeadlineIsNotExpired() {
            Booking booking = pendingBooking(NOW.plusMinutes(1), BookingStatusEnum.PENDING);
            given(bookingRepository.findForUpdateById(booking.getId())).willReturn(Optional.of(booking));

            bookingService.expireBooking(booking.getId());

            assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.PENDING);
            verify(eventService, never()).releaseTickets(any(), any(), anyInt());
        }

        @Test
        void cannotCancelSomeoneElsesBooking() {
            Booking booking = pendingBooking(NOW.plusMinutes(5), BookingStatusEnum.PENDING);
            booking.setUserId(UUID.randomUUID());
            given(bookingRepository.findForUpdateById(booking.getId())).willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelMyBooking(booking.getId()))
                    .isInstanceOf(BookingNotFoundException.class);

            verify(eventService, never()).releaseTickets(any(), any(), anyInt());
        }

        @Test
        void refundCancelsTicketsAndReleasesSeats() {
            Booking booking = pendingBooking(null, BookingStatusEnum.CONFIRMED);
            given(bookingRepository.findForUpdateById(booking.getId())).willReturn(Optional.of(booking));

            bookingService.cancelBookingAfterRefund(booking.getId());

            verify(ticketService).cancelTicketsForBooking(booking.getId());
            verify(eventService).releaseTickets(eventId, regularId, 2);
            assertThat(booking.getStatus()).isEqualTo(BookingStatusEnum.CANCELLED);
        }
    }

    @Nested
    class SalesBookingTest {

        @Test
        void reportAggregatesConfirmedSalesAndHolds() {
            given(eventService.getEventById(eventId)).willReturn(event(EventStatusEnum.PUBLISHED, new BigDecimal("20.00")));
            given(bookingRepository.aggregateSalesByEvent(eventId)).willReturn(List.of(
                    new SalesRowDto(regularId, BookingStatusEnum.CONFIRMED, 30L, new BigDecimal("600.00")),
                    new SalesRowDto(regularId, BookingStatusEnum.PENDING, 5L, new BigDecimal("100.00")),
                    new SalesRowDto(regularId, BookingStatusEnum.CANCELLED, 2L, new BigDecimal("40.00"))
            ));
            given(bookingRepository.countByEventIdAndStatus(eventId, BookingStatusEnum.CONFIRMED)).willReturn(12L);
            given(ticketService.getCheckInStats(eventId)).willReturn(new CheckInStatsDto(eventId, 30, 7, 23, 2));

            SalesReportDto report = bookingService.getSalesReport(eventId);

            verify(eventService).assertCurrentUserManagesEvent(eventId);
            assertThat(report.capacity()).isEqualTo(100);
            assertThat(report.soldTickets()).isEqualTo(30);
            assertThat(report.heldTickets()).isEqualTo(5);
            assertThat(report.revenue()).isEqualByComparingTo("600.00");
            assertThat(report.fillRatePercent()).isEqualTo(30.0);
            assertThat(report.confirmedBookings()).isEqualTo(12);
            assertThat(report.checkedIn()).isEqualTo(7);
        }
    }
}

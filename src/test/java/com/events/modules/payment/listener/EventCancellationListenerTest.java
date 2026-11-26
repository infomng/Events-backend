package com.events.modules.payment.listener;

import com.events.common.mail.IMailService;
import com.events.modules.booking.dto.BookingNoticeDto;
import com.events.modules.booking.service.IBookingService;
import com.events.modules.event.enumeration.BookingStatusEnum;
import com.events.modules.event.events.EventCancelledEvent;
import com.events.modules.payment.service.IPaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventCancellationListenerTest {

    @Mock private IBookingService bookingService;
    @Mock private IPaymentService paymentService;
    @Mock private IMailService mailService;

    @InjectMocks private EventCancellationListener listener;

    @Test
    void cancelsPendingRefundsConfirmedAndNotifiesEveryBuyer() {
        UUID eventId = UUID.randomUUID();
        UUID pending = UUID.randomUUID();
        UUID paid = UUID.randomUUID();
        UUID failing = UUID.randomUUID();
        given(bookingService.findBookingIdsForEvent(eventId, List.of(BookingStatusEnum.PENDING))).willReturn(List.of(pending));
        given(bookingService.findBookingIdsForEvent(eventId, List.of(BookingStatusEnum.CONFIRMED))).willReturn(List.of(paid, failing));
        given(paymentService.refundBookingForCancelledEvent(paid)).willReturn(true);
        given(paymentService.refundBookingForCancelledEvent(failing)).willReturn(false);
        given(bookingService.getBookingNotice(any())).willAnswer(inv ->
                new BookingNoticeDto(inv.getArgument(0), "BK-1", "Concert", "buyer@example.com", "Buyer"));

        listener.onEventCancelled(new EventCancelledEvent(eventId, "Concert"));

        verify(bookingService).cancelPendingBookingForCancelledEvent(pending);
        verify(paymentService).refundBookingForCancelledEvent(paid);
        verify(paymentService).refundBookingForCancelledEvent(failing);
        verify(mailService, times(2))
                .sendEventCancellationEmail(eq("buyer@example.com"), eq("Buyer"), eq("Concert"), eq("BK-1"), eq(true));
        verify(mailService)
                .sendEventCancellationEmail(eq("buyer@example.com"), eq("Buyer"), eq("Concert"), eq("BK-1"), eq(false));
    }
}

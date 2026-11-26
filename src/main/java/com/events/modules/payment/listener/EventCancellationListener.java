package com.events.modules.payment.listener;

import com.events.common.mail.IMailService;
import com.events.modules.booking.dto.BookingNoticeDto;
import com.events.modules.booking.service.IBookingService;
import com.events.modules.event.enumeration.BookingStatusEnum;
import com.events.modules.event.events.EventCancelledEvent;
import com.events.modules.payment.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

/**
 * Annulation d'un événement en cascade, en tâche de fond une fois l'annulation enregistrée :
 * réservations non payées annulées (places libérées, paiements en cours annulés),
 * réservations payées remboursées, billets annulés, et chaque acheteur prévenu par email.
 * Chaque réservation est traitée dans sa propre transaction : un échec n'empêche pas les autres.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventCancellationListener {

    private final IBookingService bookingService;
    private final IPaymentService paymentService;
    private final IMailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventCancelled(EventCancelledEvent event) {
        List<UUID> pending = bookingService.findBookingIdsForEvent(event.eventId(), List.of(BookingStatusEnum.PENDING));
        List<UUID> confirmed = bookingService.findBookingIdsForEvent(event.eventId(), List.of(BookingStatusEnum.CONFIRMED));
        log.info("Cancelling event {}: {} pending and {} confirmed booking(s) to process",
                event.eventId(), pending.size(), confirmed.size());

        int refundFailures = 0;
        for (UUID bookingId : pending) {
            try {
                bookingService.cancelPendingBookingForCancelledEvent(bookingId);
                notifyBuyer(bookingId, true);
            } catch (RuntimeException e) {
                log.error("Failed to cancel pending booking {} of cancelled event {}", bookingId, event.eventId(), e);
            }
        }
        for (UUID bookingId : confirmed) {
            try {
                boolean refunded = paymentService.refundBookingForCancelledEvent(bookingId);
                if (!refunded) {
                    refundFailures++;
                }
                notifyBuyer(bookingId, refunded);
            } catch (RuntimeException e) {
                refundFailures++;
                log.error("Failed to refund booking {} of cancelled event {}", bookingId, event.eventId(), e);
            }
        }

        if (refundFailures > 0) {
            log.error("Event {} cancelled with {} refund(s) to handle manually (failureReason REFUND_REQUIRED)",
                    event.eventId(), refundFailures);
        }
    }

    private void notifyBuyer(UUID bookingId, boolean refunded) {
        BookingNoticeDto notice = bookingService.getBookingNotice(bookingId);
        mailService.sendEventCancellationEmail(notice.userEmail(), notice.userFullName(), notice.eventName(),
                notice.bookingReference(), refunded);
    }
}

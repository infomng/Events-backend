package com.events.modules.booking.listener;

import com.events.common.mail.IMailService;
import com.events.modules.booking.events.BookingConfirmedEvent;
import com.events.modules.ticket.dto.TicketDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Envoie l'email de confirmation une fois la transaction validée :
 * aucun email n'est envoyé pour une commande finalement annulée par un rollback.
 */
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private final IMailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Map<String, byte[]> qrCodes = new LinkedHashMap<>();
        for (TicketDto ticket : event.tickets()) {
            qrCodes.put(ticket.ticketCode(), ticket.qrCode());
        }
        mailService.sendBookingConfirmationEmail(
                event.userEmail(),
                event.userFullName(),
                event.eventName(),
                event.bookingReference(),
                qrCodes
        );
    }
}

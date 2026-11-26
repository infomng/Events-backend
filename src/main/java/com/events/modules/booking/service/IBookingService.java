package com.events.modules.booking.service;

import com.events.modules.booking.dto.BookingPaymentDto;

import java.util.UUID;

public interface IBookingService {

    /**
     * Retourne une réservation payable appartenant à l'utilisateur.
     * Échoue si la réservation n'existe pas, n'appartient pas à l'utilisateur
     * ou n'est plus en attente de paiement.
     */
    BookingPaymentDto getPayableBooking(UUID bookingId, UUID userId);

    /**
     * Marque la réservation comme payée et confirmée.
     */
    void confirmBookingPayment(UUID bookingId, String paymentId);

    /**
     * Annule la réservation suite à un remboursement.
     */
    void cancelBookingAfterRefund(UUID bookingId);
}
